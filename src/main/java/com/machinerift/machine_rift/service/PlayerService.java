package com.machinerift.machine_rift.service;

import com.machinerift.machine_rift.dto.PlayerResponseDto;
import com.machinerift.machine_rift.mapper.PlayerMapper;
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

}
