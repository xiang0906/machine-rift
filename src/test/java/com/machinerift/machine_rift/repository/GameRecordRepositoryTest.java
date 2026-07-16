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
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class GameRecordRepositoryTest {

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private StageRepository stageRepository;

    @Test
    void shouldOrderRankingsAndDetectRelatedGameRecords() {
        Player player = playerRepository.save(Player.builder()
                .playerName("Alice")
                .level(1)
                .createdAt(LocalDateTime.now())
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

        List<Integer> scores = gameRecordRepository.findAllByOrderByScoreDesc().stream()
                .map(GameRecord::getScore)
                .toList();

        assertEquals(List.of(900, 100), scores);
        assertTrue(gameRecordRepository.existsByPlayerPlayerId(player.getPlayerId()));
        assertTrue(gameRecordRepository.existsByStageStageId(stage.getStageId()));
    }
}
