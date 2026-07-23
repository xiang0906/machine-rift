package com.machinerift.machine_rift.service;

import com.machinerift.machine_rift.dto.PlayerRequestDto;
import com.machinerift.machine_rift.dto.PlayerResponseDto;
import com.machinerift.machine_rift.entity.Player;
import com.machinerift.machine_rift.exception.ResourceConflictException;
import com.machinerift.machine_rift.exception.ResourceNotFoundException;
import com.machinerift.machine_rift.mapper.PlayerMapper;
import com.machinerift.machine_rift.repository.GameRecordRepository;
import com.machinerift.machine_rift.repository.PlayerRepository;
import com.machinerift.machine_rift.repository.PlayerSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

/**
 * Service layer for player-related operations.
 */
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final GameRecordRepository gameRecordRepository;
    private final PlayerMapper playerMapper;
    private final PlayerSessionRepository playerSessionRepository;

    /**
     * Retrieves all players.
     *
     * @return list of player response DTOs
     */
    @Transactional(readOnly = true)
    public List<PlayerResponseDto> getAllPlayers() {
        return playerRepository.findAll().stream()
                .map(playerMapper::toResponseDto)
                .toList();
    }

    /**
     * Retrieves a player by id.
     *
     * @param id player id
     * @return player response DTO
     */
    @Transactional(readOnly = true)
    public PlayerResponseDto getPlayerById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到玩家，ID：" + id));
        return playerMapper.toResponseDto(player);
    }

    /**
     * Updates an existing player.
     *
     * @param id player id
     * @param requestDto update payload
     * @return updated player response DTO
     */
    @Transactional
    public PlayerResponseDto updatePlayer(Long id, PlayerRequestDto requestDto) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到玩家，ID：" + id));
        String playerName = requestDto.getPlayerName().trim();
        if (playerRepository.existsByPlayerNameIgnoreCaseAndPlayerIdNot(playerName, id)) {
            throw new ResourceConflictException("此玩家名稱已被使用");
        }
        playerMapper.updateEntity(player, requestDto);
        player.setPlayerName(playerName);
        try {
            return playerMapper.toResponseDto(playerRepository.saveAndFlush(player));
        } catch (DataIntegrityViolationException exception) {
            throw new ResourceConflictException("此玩家名稱已被使用");
        }
    }

    /**
     * Deletes a player by id.
     *
     * @param id player id
     */
    @Transactional
    public void deletePlayer(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new ResourceNotFoundException("找不到玩家，ID：" + id);
        }
        if (gameRecordRepository.existsByPlayerPlayerId(id)) {
            throw new ResourceConflictException("玩家已有遊戲紀錄，無法刪除");
        }
        playerSessionRepository.deleteAllByPlayerPlayerId(id);
        playerRepository.deleteById(id);
    }
}
