package com.machinerift.machine_rift.service;

import com.machinerift.machine_rift.dto.GameRecordRequestDto;
import com.machinerift.machine_rift.dto.GameRecordResponseDto;
import com.machinerift.machine_rift.dto.RankingResponseDto;
import com.machinerift.machine_rift.entity.GameRecord;
import com.machinerift.machine_rift.entity.Player;
import com.machinerift.machine_rift.entity.Stage;
import com.machinerift.machine_rift.exception.ResourceConflictException;
import com.machinerift.machine_rift.exception.ResourceNotFoundException;
import com.machinerift.machine_rift.mapper.GameRecordMapper;
import com.machinerift.machine_rift.repository.GameRecordRepository;
import com.machinerift.machine_rift.repository.StageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameRecordServiceTest {

    @Mock
    private GameRecordRepository gameRecordRepository;

    @Mock
    private StageRepository stageRepository;

    @Mock
    private PlayerProgressService playerProgressService;

    private GameRecordService gameRecordService;

    @BeforeEach
    void setUp() {
        gameRecordService = new GameRecordService(
                gameRecordRepository, stageRepository, new GameRecordMapper(),
                playerProgressService);
    }

    @Test
    void shouldSaveRecordWithExistingPlayerAndStage() {
        Player player = player(1L);
        Stage stage = stage(2L);
        GameRecordRequestDto request = GameRecordRequestDto.builder()
                .stageId(2L).score(900).result("WIN").playTime(120).build();
        when(stageRepository.findById(2L)).thenReturn(Optional.of(stage));
        when(playerProgressService.isStageUnlocked(player, stage)).thenReturn(true);
        when(gameRecordRepository.save(any(GameRecord.class))).thenAnswer(invocation -> {
            GameRecord record = invocation.getArgument(0);
            record.setRecordId(10L);
            return record;
        });

        GameRecordResponseDto response = gameRecordService.saveGameRecord(request, player);
        ArgumentCaptor<GameRecord> savedRecord = ArgumentCaptor.forClass(GameRecord.class);
        verify(gameRecordRepository).save(savedRecord.capture());
        verify(playerProgressService).recordGameResult(player, stage, 900, "WIN", 120);

        assertEquals(10L, response.getRecordId());
        assertEquals(1L, savedRecord.getValue().getPlayer().getPlayerId());
        assertEquals(2L, savedRecord.getValue().getStage().getStageId());
    }

    @Test
    void shouldReturnChineseMessageWhenStageDoesNotExist() {
        Player player = player(1L);
        GameRecordRequestDto request = GameRecordRequestDto.builder()
                .stageId(99L).score(900).result("WIN").playTime(120).build();
        when(stageRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> gameRecordService.saveGameRecord(request, player));

        assertEquals("找不到指定的關卡，ID：99", exception.getMessage());
    }

    @Test
    void shouldReturnChineseMessageWhenStageIsLocked() {
        Player player = player(1L);
        Stage stage = stage(2L);
        GameRecordRequestDto request = GameRecordRequestDto.builder()
                .stageId(2L).score(900).result("WIN").playTime(120).build();
        when(stageRepository.findById(2L)).thenReturn(Optional.of(stage));
        when(playerProgressService.isStageUnlocked(player, stage)).thenReturn(false);

        ResourceConflictException exception = assertThrows(
                ResourceConflictException.class,
                () -> gameRecordService.saveGameRecord(request, player));

        assertEquals("此關卡尚未解鎖", exception.getMessage());
    }

    @Test
    void shouldReturnLatestRecordsForAuthenticatedPlayer() {
        Player player = player(1L);
        Stage stage = stage(2L);
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 6, 14, 30);
        GameRecord record = record(10L, player, stage, 900, 80);
        record.setCreatedAt(createdAt);
        when(gameRecordRepository.findTop20ByPlayerOrderByCreatedAtDescRecordIdDesc(player))
                .thenReturn(List.of(record));

        var history = gameRecordService.getPlayerHistory(player);

        assertEquals(1, history.size());
        assertEquals("Stage 2", history.getFirst().getStageName());
        assertEquals(900, history.getFirst().getScore());
        assertEquals(createdAt, history.getFirst().getCreatedAt());
        verify(gameRecordRepository)
                .findTop20ByPlayerOrderByCreatedAtDescRecordIdDesc(player);
    }

    @Test
    void shouldReturnEachPlayersBestRecordAndLimitRankingToTen() {
        List<GameRecord> orderedRecords = List.of(
                record(1L, player(2L), stage(2L), 1200, 50),
                record(2L, player(1L), stage(1L), 1200, 80),
                record(3L, player(1L), stage(2L), 1000, 40),
                record(4L, player(3L), stage(1L), 900, 60),
                record(5L, player(4L), stage(1L), 800, 60),
                record(6L, player(5L), stage(1L), 700, 60),
                record(7L, player(6L), stage(1L), 600, 60),
                record(8L, player(7L), stage(1L), 500, 60),
                record(9L, player(8L), stage(1L), 400, 60),
                record(10L, player(9L), stage(1L), 300, 60),
                record(11L, player(10L), stage(1L), 200, 60),
                record(12L, player(11L), stage(1L), 100, 60),
                record(13L, player(12L), stage(1L), 50, 60));
        when(gameRecordRepository.findAllByOrderByScoreDescPlayTimeAscRecordIdAsc())
                .thenReturn(orderedRecords);

        RankingResponseDto ranking = gameRecordService.getRankings(null);

        assertEquals(12, ranking.getParticipantCount());
        assertEquals(13, ranking.getTotalGameCount());
        assertEquals(10, ranking.getEntries().size());
        assertEquals(List.of("Player 2", "Player 1", "Player 3"),
                ranking.getEntries().stream()
                        .limit(3)
                        .map(entry -> entry.getPlayerName())
                        .toList());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
                ranking.getEntries().stream().map(entry -> entry.getRank()).toList());
        assertEquals("Stage 2", ranking.getEntries().getFirst().getStageName());
    }

    @Test
    void shouldReturnEachPlayersBestRecordForSelectedStage() {
        Stage selectedStage = stage(2L);
        List<GameRecord> orderedRecords = List.of(
                record(1L, player(1L), selectedStage, 900, 80),
                record(2L, player(2L), selectedStage, 900, 90),
                record(3L, player(1L), selectedStage, 700, 40));
        when(stageRepository.findById(2L)).thenReturn(Optional.of(selectedStage));
        when(gameRecordRepository.findAllByStageOrderByScoreDescPlayTimeAscRecordIdAsc(selectedStage))
                .thenReturn(orderedRecords);

        RankingResponseDto ranking = gameRecordService.getRankings(2L);

        assertEquals(2, ranking.getParticipantCount());
        assertEquals(3, ranking.getTotalGameCount());
        assertEquals(List.of("Player 1", "Player 2"),
                ranking.getEntries().stream().map(entry -> entry.getPlayerName()).toList());
        assertEquals(List.of(1, 2),
                ranking.getEntries().stream().map(entry -> entry.getRank()).toList());
        verify(gameRecordRepository)
                .findAllByStageOrderByScoreDescPlayTimeAscRecordIdAsc(selectedStage);
    }

    @Test
    void shouldRejectRankingFilterForMissingStage() {
        when(stageRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> gameRecordService.getRankings(99L));

        assertEquals("找不到指定的關卡，ID：99", exception.getMessage());
    }

    private Player player(Long id) {
        return Player.builder().playerId(id).playerName("Player " + id).build();
    }

    private Stage stage(Long id) {
        return Stage.builder().stageId(id).stageName("Stage " + id).difficulty("Normal")
                .rewardGold(10).enemyCount(1).build();
    }

    private GameRecord record(
            Long id, Player player, Stage stage, int score, int playTime) {
        return GameRecord.builder().recordId(id).player(player).stage(stage)
                .score(score).result("WIN").playTime(playTime).build();
    }
}
