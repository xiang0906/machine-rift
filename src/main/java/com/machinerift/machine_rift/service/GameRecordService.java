package com.machinerift.machine_rift.service;

import com.machinerift.machine_rift.dto.GameRecordRequestDto;
import com.machinerift.machine_rift.dto.GameRecordResponseDto;
import com.machinerift.machine_rift.dto.RankingEntryResponseDto;
import com.machinerift.machine_rift.dto.RankingResponseDto;
import com.machinerift.machine_rift.entity.GameRecord;
import com.machinerift.machine_rift.entity.Player;
import com.machinerift.machine_rift.entity.Stage;
import com.machinerift.machine_rift.exception.ResourceNotFoundException;
import com.machinerift.machine_rift.exception.ResourceConflictException;
import com.machinerift.machine_rift.mapper.GameRecordMapper;
import com.machinerift.machine_rift.repository.GameRecordRepository;
import com.machinerift.machine_rift.repository.StageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service layer for game record persistence and ranking queries.
 */
@Service
@RequiredArgsConstructor
public class GameRecordService {

    private static final int RANKING_LIMIT = 10;

    private final GameRecordRepository gameRecordRepository;
    private final StageRepository stageRepository;
    private final GameRecordMapper gameRecordMapper;
    private final PlayerProgressService playerProgressService;

    /**
     * Saves a completed game session.
     *
     * @param requestDto incoming game record payload
     * @param player authenticated player resolved from the access token
     * @return saved response DTO
     */
    @Transactional
    public GameRecordResponseDto saveGameRecord(
            GameRecordRequestDto requestDto, Player player) {
        Stage stage = stageRepository.findById(requestDto.getStageId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "找不到指定的關卡，ID：" + requestDto.getStageId()));
        if (!playerProgressService.isStageUnlocked(player, stage)) {
            throw new ResourceConflictException("此關卡尚未解鎖");
        }

        GameRecord savedRecord = gameRecordRepository.save(gameRecordMapper.toEntity(requestDto, player, stage));
        playerProgressService.recordGameResult(
                player,
                stage,
                requestDto.getScore(),
                requestDto.getResult(),
                requestDto.getPlayTime());
        return gameRecordMapper.toResponseDto(savedRecord);
    }

    /**
     * Returns at most ten players, using each player's best score and shortest
     * play time as the tie breaker.
     */
    @Transactional(readOnly = true)
    public RankingResponseDto getRankings() {
        List<GameRecord> orderedRecords =
                gameRecordRepository.findAllByOrderByScoreDescPlayTimeAscRecordIdAsc();
        Set<Long> participantIds = new HashSet<>();
        List<RankingEntryResponseDto> entries = new ArrayList<>();

        for (GameRecord record : orderedRecords) {
            Long playerId = record.getPlayer().getPlayerId();
            boolean firstRecordForPlayer = participantIds.add(playerId);
            if (!firstRecordForPlayer || entries.size() >= RANKING_LIMIT) {
                continue;
            }
            entries.add(RankingEntryResponseDto.builder()
                    .rank(entries.size() + 1)
                    .playerName(record.getPlayer().getPlayerName())
                    .stageName(record.getStage().getStageName())
                    .score(record.getScore())
                    .playTime(record.getPlayTime())
                    .result(record.getResult())
                    .build());
        }

        return RankingResponseDto.builder()
                .participantCount(participantIds.size())
                .totalGameCount(orderedRecords.size())
                .entries(entries)
                .build();
    }
}
