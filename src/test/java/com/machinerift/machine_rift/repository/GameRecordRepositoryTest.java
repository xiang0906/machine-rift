package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.GameRecord;
import com.machinerift.machine_rift.entity.Player;
import com.machinerift.machine_rift.entity.Stage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class GameRecordRepositoryTest {

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private StageRepository stageRepository;

    @Test
    void shouldOrderRankingsByScoreThenShortestPlayTime() {
        Player player = playerRepository.save(Player.builder()
                .playerName("Alice")
                .username("alice")
                .passwordHash("test-password-hash")
                .level(1)
                .experience(0)
                .gold(0)
                .completedStages(0)
                .unlockedTowerCount(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        Stage stage = stageRepository.save(Stage.builder()
                .stageName("Stage 1")
                .difficulty("Normal")
                .rewardGold(100)
                .enemyCount(10)
                .build());

        gameRecordRepository.save(GameRecord.builder()
                .player(player).stage(stage).score(100).result("WIN").playTime(30).build());
        gameRecordRepository.save(GameRecord.builder()
                .player(player).stage(stage).score(900).result("WIN").playTime(40).build());
        gameRecordRepository.save(GameRecord.builder()
                .player(player).stage(stage).score(900).result("WIN").playTime(20).build());

        List<String> rankingOrder = gameRecordRepository
                .findAllByOrderByScoreDescPlayTimeAscRecordIdAsc().stream()
                .map(record -> record.getScore() + ":" + record.getPlayTime())
                .toList();

        assertEquals(List.of("900:20", "900:40", "100:30"), rankingOrder);
    }
}
