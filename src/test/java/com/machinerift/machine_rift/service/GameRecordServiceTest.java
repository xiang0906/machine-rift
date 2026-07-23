package com.machinerift.machine_rift.service;

import com.machinerift.machine_rift.dto.GameRecordRequestDto;
import com.machinerift.machine_rift.dto.GameRecordResponseDto;
import com.machinerift.machine_rift.entity.GameRecord;
import com.machinerift.machine_rift.entity.Player;
import com.machinerift.machine_rift.entity.Stage;
import com.machinerift.machine_rift.mapper.GameRecordMapper;
import com.machinerift.machine_rift.repository.GameRecordRepository;
import com.machinerift.machine_rift.repository.PlayerRepository;
import com.machinerift.machine_rift.repository.StageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameRecordServiceTest {

    @Mock
    private GameRecordRepository gameRecordRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private StageRepository stageRepository;

    @Mock
    private PlayerProgressService playerProgressService;

    private GameRecordService gameRecordService;

    @BeforeEach
    void setUp() {
        gameRecordService = new GameRecordService(
                gameRecordRepository, playerRepository, stageRepository,
                new GameRecordMapper(), playerProgressService);
    }

    @Test
    void shouldSaveRecordWithExistingPlayerAndStage() {
        Player player = player(1L);
        Stage stage = stage(2L);
        GameRecordRequestDto request = GameRecordRequestDto.builder()
                .playerId(1L).stageId(2L).score(900).result("WIN").playTime(120).build();
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(stageRepository.findById(2L)).thenReturn(Optional.of(stage));
        when(playerProgressService.isStageUnlocked(player, stage)).thenReturn(true);
        when(gameRecordRepository.save(any(GameRecord.class))).thenAnswer(invocation -> {
            GameRecord record = invocation.getArgument(0);
            record.setRecordId(10L);
            return record;
        });

        GameRecordResponseDto response = gameRecordService.saveGameRecord(request);
        ArgumentCaptor<GameRecord> savedRecord = ArgumentCaptor.forClass(GameRecord.class);
        verify(gameRecordRepository).save(savedRecord.capture());
        verify(playerProgressService).recordGameResult(player, stage, 900, "WIN", 120);

        assertEquals(10L, response.getRecordId());
        assertEquals(1L, savedRecord.getValue().getPlayer().getPlayerId());
        assertEquals(2L, savedRecord.getValue().getStage().getStageId());
    }

    @Test
    void shouldReturnRankingsInRepositoryScoreOrder() {
        GameRecord highScore = record(1L, 1200);
        GameRecord lowScore = record(2L, 400);
        when(gameRecordRepository.findAllByOrderByScoreDesc()).thenReturn(List.of(highScore, lowScore));

        List<GameRecordResponseDto> rankings = gameRecordService.getRankings();

        assertEquals(List.of(1200, 400), rankings.stream().map(GameRecordResponseDto::getScore).toList());
    }

    private Player player(Long id) {
        return Player.builder().playerId(id).playerName("Player " + id).level(1).build();
    }

    private Stage stage(Long id) {
        return Stage.builder().stageId(id).stageName("Stage " + id).difficulty("Normal")
                .rewardGold(10).enemyCount(1).build();
    }

    private GameRecord record(Long id, int score) {
        return GameRecord.builder().recordId(id).player(player(1L)).stage(stage(1L))
                .score(score).result("WIN").playTime(60).build();
    }
}
