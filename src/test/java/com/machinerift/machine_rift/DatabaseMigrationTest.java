package com.machinerift.machine_rift;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseMigrationTest {

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
