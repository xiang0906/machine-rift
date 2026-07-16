package com.machinerift.machine_rift.service;

import com.machinerift.machine_rift.dto.GameRecordRequestDto;
import com.machinerift.machine_rift.dto.GameRecordResponseDto;
import com.machinerift.machine_rift.entity.GameRecord;
import com.machinerift.machine_rift.entity.Player;
import com.machinerift.machine_rift.entity.Stage;
import com.machinerift.machine_rift.exception.ResourceNotFoundException;
import com.machinerift.machine_rift.mapper.GameRecordMapper;
import com.machinerift.machine_rift.repository.GameRecordRepository;
import com.machinerift.machine_rift.repository.PlayerRepository;
import com.machinerift.machine_rift.repository.StageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for game record persistence and ranking queries.
 */
@Service
@RequiredArgsConstructor
public class GameRecordService {

    private final GameRecordRepository gameRecordRepository;
    private final PlayerRepository playerRepository;
    private final StageRepository stageRepository;
    private final GameRecordMapper gameRecordMapper;

    /**
     * Saves a completed game session.
     *
     * @param requestDto incoming game record payload
     * @return saved response DTO
     */
    @Transactional
    public GameRecordResponseDto saveGameRecord(GameRecordRequestDto requestDto) {
        Player player = playerRepository.findById(requestDto.getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + requestDto.getPlayerId()));
        Stage stage = stageRepository.findById(requestDto.getStageId())
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found with id: " + requestDto.getStageId()));

        GameRecord savedRecord = gameRecordRepository.save(gameRecordMapper.toEntity(requestDto, player, stage));
        return gameRecordMapper.toResponseDto(savedRecord);
    }

    /**
     * Retrieves all game records.
     *
     * @return list of response DTOs
     */
    @Transactional(readOnly = true)
    public List<GameRecordResponseDto> getAllGameRecords() {
        return gameRecordRepository.findAll().stream()
                .map(gameRecordMapper::toResponseDto)
                .toList();
    }

    /**
     * Retrieves a game record by id.
     *
     * @param id record id
     * @return response DTO
     */
    @Transactional(readOnly = true)
    public GameRecordResponseDto getGameRecordById(Long id) {
        GameRecord gameRecord = gameRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game record not found with id: " + id));
        return gameRecordMapper.toResponseDto(gameRecord);
    }

    /**
     * Retrieves top-ranked game records.
     *
     * @return top-scored game records
     */
    @Transactional(readOnly = true)
    public List<GameRecordResponseDto> getRankings() {
        return gameRecordRepository.findAllByOrderByScoreDesc().stream()
                .map(gameRecordMapper::toResponseDto)
                .toList();
    }
}
