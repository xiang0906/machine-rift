package com.machinerift.machine_rift;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DatabaseMigrationTest {

    @Test
    void v21AddsTenSpeedToStandardEnemiesAndKeepsDroneSpeed() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(
                "jdbc:h2:mem:migration-v21;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("root");
        dataSource.setPassword("");

        Flyway.configure()
                .dataSource(dataSource)
                .target(MigrationVersion.fromVersion("20"))
                .load()
                .migrate();

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        Flyway.configure().dataSource(dataSource).load().migrate();

        assertEquals(92.0, enemySpeed(jdbc, "疾風無人機"));
        assertEquals(72.0, enemySpeed(jdbc, "偵察機"));
        assertEquals(55.0, enemySpeed(jdbc, "裝甲機"));
        assertEquals(50.0, enemySpeed(jdbc, "護盾機兵"));
        assertEquals(48.0, enemySpeed(jdbc, "裂隙核心"));
        assertEquals(39.0, enemySpeed(jdbc, "裂隙巨像"));
    }

    @Test
    void v20SpeedsUpDurableEnemiesWithoutChangingEarlyEnemySpeed() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(
                "jdbc:h2:mem:migration-v20;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("root");
        dataSource.setPassword("");

        Flyway.configure()
                .dataSource(dataSource)
                .target(MigrationVersion.fromVersion("19"))
                .load()
                .migrate();

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        Flyway.configure()
                .dataSource(dataSource)
                .target(MigrationVersion.fromVersion("20"))
                .load()
                .migrate();

        assertEquals(62.0, enemySpeed(jdbc, "偵察機"));
        assertEquals(45.0, enemySpeed(jdbc, "裝甲機"));
        assertEquals(92.0, enemySpeed(jdbc, "疾風無人機"));
        assertEquals(40.0, enemySpeed(jdbc, "護盾機兵"));
        assertEquals(38.0, enemySpeed(jdbc, "裂隙核心"));
        assertEquals(29.0, enemySpeed(jdbc, "裂隙巨像"));
    }

    private double enemySpeed(JdbcTemplate jdbc, String enemyName) {
        return jdbc.queryForObject(
                "SELECT speed FROM enemy WHERE enemy_name = ?", Double.class, enemyName);
    }

    @Test
    void v19RemovesUnusedLevelAndExperienceWhilePreservingPlayerData() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(
                "jdbc:h2:mem:migration-v19;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("root");
        dataSource.setPassword("");

        Flyway.configure()
                .dataSource(dataSource)
                .target(MigrationVersion.fromVersion("18"))
                .load()
                .migrate();

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("""
                INSERT INTO player (
                  player_id, player_name, username, password_hash, level, experience,
                  gold, completed_stages, created_at, updated_at
                ) VALUES (
                  996, 'Lean Player', 'lean_player', 'password-hash', 4, 3450,
                  780, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """);

        Flyway.configure().dataSource(dataSource).load().migrate();

        assertEquals(0, jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = 'PUBLIC'
                  AND TABLE_NAME = 'PLAYER'
                  AND COLUMN_NAME IN ('LEVEL', 'EXPERIENCE')
                """, Integer.class));
        assertEquals(780, jdbc.queryForObject(
                "SELECT gold FROM player WHERE player_id = 996", Integer.class));
        assertEquals(3, jdbc.queryForObject(
                "SELECT completed_stages FROM player WHERE player_id = 996", Integer.class));
    }

    @Test
    void v18GraduallyIncreasesStagePressureAndKeepsSummariesSynchronized() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(
                "jdbc:h2:mem:migration-v18;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("root");
        dataSource.setPassword("");

        Flyway.configure()
                .dataSource(dataSource)
                .target(MigrationVersion.fromVersion("17"))
                .load()
                .migrate();

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        Flyway.configure().dataSource(dataSource).load().migrate();

        assertEquals(List.of(10, 19, 28, 35, 42, 51), jdbc.queryForList("""
                SELECT enemy_count FROM stage ORDER BY stage_id
                """, Integer.class));
        assertEquals(List.of(5, 5), waveValues(jdbc, "裂隙前線", "enemy_count"));
        assertEquals(List.of(6, 6, 7), waveValues(jdbc, "機械迴廊", "enemy_count"));
        assertEquals(List.of(8, 8, 8, 4), waveValues(jdbc, "核心裂谷", "enemy_count"));
        assertEquals(List.of(7, 7, 7, 7, 7), waveValues(jdbc, "熔火交叉口", "enemy_count"));
        assertEquals(List.of(585, 585, 648, 720, 810),
                waveValues(jdbc, "熔火交叉口", "spawn_interval_ms"));
        assertEquals(List.of(8, 8, 8, 7, 7, 4), waveValues(jdbc, "量子迷城", "enemy_count"));
        assertEquals(List.of(510, 550, 600, 670, 750, 970),
                waveValues(jdbc, "量子迷城", "spawn_interval_ms"));
        assertEquals(List.of(10, 10, 10, 9, 7, 5), waveValues(jdbc, "機神核心", "enemy_count"));
        assertEquals(List.of(430, 460, 530, 600, 660, 850),
                waveValues(jdbc, "機神核心", "spawn_interval_ms"));
        assertEquals(165, jdbc.queryForObject(
                "SELECT health FROM enemy WHERE enemy_name = '裂隙核心'", Integer.class));
        assertEquals(286, jdbc.queryForObject(
                "SELECT health FROM enemy WHERE enemy_name = '裂隙巨像'", Integer.class));
    }

    private List<Integer> waveValues(JdbcTemplate jdbc, String stageName, String column) {
        return jdbc.queryForList("""
                SELECT %s
                FROM stage_wave
                WHERE stage_id = (SELECT stage_id FROM stage WHERE stage_name = ?)
                ORDER BY wave_number
                """.formatted(column), Integer.class, stageName);
    }

    @Test
    void v15AddsCreationTimeToExistingGameRecords() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(
                "jdbc:h2:mem:migration-v15;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("root");
        dataSource.setPassword("");

        Flyway.configure()
                .dataSource(dataSource)
                .target(MigrationVersion.fromVersion("14"))
                .load()
                .migrate();

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("""
                INSERT INTO player (
                  player_id, player_name, level, created_at, username, password_hash
                ) VALUES (
                  998, 'History Player', 1, CURRENT_TIMESTAMP,
                  'history_player', 'history-password-hash'
                )
                """);
        jdbc.update("""
                INSERT INTO game_record (
                  record_id, player_id, stage_id, score, result, play_time
                ) VALUES (998, 998, 1, 860, 'WIN', 95)
                """);

        Flyway.configure()
                .dataSource(dataSource)
                .target(MigrationVersion.fromVersion("15"))
                .load()
                .migrate();

        assertNotNull(jdbc.queryForObject(
                "SELECT created_at FROM game_record WHERE record_id = 998",
                java.sql.Timestamp.class));
        assertEquals(1, jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.INDEXES
                WHERE TABLE_SCHEMA = 'PUBLIC'
                  AND INDEX_NAME = 'IDX_GAME_RECORD_PLAYER_CREATED_AT'
                """, Integer.class));
    }

    @Test
    void v16RestoresExactTowerUnlockRowsAndRemovesAggregateCount() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(
                "jdbc:h2:mem:migration-v16;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("root");
        dataSource.setPassword("");

        Flyway.configure()
                .dataSource(dataSource)
                .target(MigrationVersion.fromVersion("15"))
                .load()
                .migrate();

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("""
                INSERT INTO player (
                  player_id, player_name, level, created_at, username, password_hash,
                  experience, gold, completed_stages, unlocked_tower_count, updated_at
                ) VALUES (
                  997, 'Tower Player', 2, CURRENT_TIMESTAMP,
                  'tower_player', 'tower-password-hash', 1200, 300, 1, 3, CURRENT_TIMESTAMP
                )
                """);

        Flyway.configure()
                .dataSource(dataSource)
                .target(MigrationVersion.fromVersion("16"))
                .load()
                .migrate();

        assertEquals(List.of("脈衝砲塔", "離子機槍塔", "量子砲塔"), jdbc.queryForList("""
                SELECT t.tower_name
                FROM player_tower_unlock ptu
                JOIN tower t ON t.tower_id = ptu.tower_id
                WHERE ptu.player_id = 997
                ORDER BY t.cost, t.tower_id
                """, String.class));
        assertEquals(0, jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = 'PUBLIC'
                  AND TABLE_NAME = 'PLAYER'
                  AND COLUMN_NAME = 'UNLOCKED_TOWER_COUNT'
                """, Integer.class));
    }

    @Test
    void v17AddsPermanentUnlockCostsToEveryTower() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(
                "jdbc:h2:mem:migration-v17;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("root");
        dataSource.setPassword("");

        Flyway.configure()
                .dataSource(dataSource)
                .target(MigrationVersion.fromVersion("16"))
                .load()
                .migrate();

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate();

        assertEquals(List.of(0, 250, 450, 650, 900, 1200), jdbc.queryForList("""
                SELECT unlock_cost
                FROM tower
                ORDER BY cost, tower_id
                """, Integer.class));
    }

    @Test
    void v14PreservesProgressTowerCountAndLatestSession() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(
                "jdbc:h2:mem:migration-v14;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("root");
        dataSource.setPassword("");

        Flyway.configure()
                .dataSource(dataSource)
                .target(MigrationVersion.fromVersion("13"))
                .load()
                .migrate();

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("""
                INSERT INTO player (
                  player_id, player_name, level, created_at, username, password_hash
                ) VALUES (
                  999, 'Migration Player', 3, CURRENT_TIMESTAMP,
                  'migration_player', 'migration-password-hash'
                )
                """);
        jdbc.update("""
                INSERT INTO player_progress (
                  player_id, experience, gold, completed_stages, updated_at
                ) VALUES (999, 2500, 360, 2, CURRENT_TIMESTAMP)
                """);
        jdbc.update("""
                INSERT INTO player_tower_unlock (
                  player_id, tower_id, unlocked_at
                )
                SELECT 999, tower_id, CURRENT_TIMESTAMP
                FROM tower
                ORDER BY cost, tower_id
                LIMIT 2
                """);
        jdbc.update("""
                INSERT INTO player_session (
                  player_id, token_hash, created_at, expires_at
                ) VALUES
                  (999, ?, TIMESTAMP '2026-07-01 10:00:00', TIMESTAMP '2026-08-01 10:00:00'),
                  (999, ?, TIMESTAMP '2026-07-02 10:00:00', TIMESTAMP '2026-08-02 10:00:00')
                """, "a".repeat(64), "b".repeat(64));

        Flyway.configure()
                .dataSource(dataSource)
                .target(MigrationVersion.fromVersion("14"))
                .load()
                .migrate();

        assertEquals(2500, jdbc.queryForObject(
                "SELECT experience FROM player WHERE player_id = 999", Integer.class));
        assertEquals(360, jdbc.queryForObject(
                "SELECT gold FROM player WHERE player_id = 999", Integer.class));
        assertEquals(2, jdbc.queryForObject(
                "SELECT completed_stages FROM player WHERE player_id = 999", Integer.class));
        assertEquals(2, jdbc.queryForObject(
                "SELECT unlocked_tower_count FROM player WHERE player_id = 999", Integer.class));
        assertEquals("b".repeat(64), jdbc.queryForObject(
                "SELECT session_token_hash FROM player WHERE player_id = 999", String.class));
        assertEquals(0, jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = 'PUBLIC'
                  AND LOWER(TABLE_NAME) IN (
                    'player_progress', 'player_session', 'player_tower_unlock'
                  )
                """, Integer.class));
    }
}
