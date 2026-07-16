package com.machinerift.machine_rift.service;

import com.machinerift.machine_rift.dto.PlayerRequestDto;
import com.machinerift.machine_rift.dto.PlayerResponseDto;
import com.machinerift.machine_rift.entity.Player;
import com.machinerift.machine_rift.exception.ResourceConflictException;
import com.machinerift.machine_rift.exception.ResourceNotFoundException;
import com.machinerift.machine_rift.mapper.PlayerMapper;
import com.machinerift.machine_rift.repository.GameRecordRepository;
import com.machinerift.machine_rift.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));
        return playerMapper.toResponseDto(player);
    }

    /**
     * Creates a new player.
     *
     * @param requestDto player creation payload
     * @return created player response DTO
     */
    @Transactional
    public PlayerResponseDto createPlayer(PlayerRequestDto requestDto) {
        Player player = playerMapper.toEntity(requestDto);
        Player savedPlayer = playerRepository.save(player);
        return playerMapper.toResponseDto(savedPlayer);
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
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));
        playerMapper.updateEntity(player, requestDto);
        return playerMapper.toResponseDto(playerRepository.save(player));
    }

    /**
     * Deletes a player by id.
     *
     * @param id player id
     */
    @Transactional
    public void deletePlayer(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Player not found with id: " + id);
        }
        if (gameRecordRepository.existsByPlayerPlayerId(id)) {
            throw new ResourceConflictException("Cannot delete player with existing game records.");
        }
        playerRepository.deleteById(id);
    }
}
