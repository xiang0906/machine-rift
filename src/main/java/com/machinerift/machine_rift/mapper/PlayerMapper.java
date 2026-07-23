package com.machinerift.machine_rift.mapper;

import com.machinerift.machine_rift.dto.PlayerRequestDto;
import com.machinerift.machine_rift.dto.PlayerResponseDto;
import com.machinerift.machine_rift.entity.Player;
import org.springframework.stereotype.Component;

/**
 * Converts between player entities and DTOs.
 */
@Component
public class PlayerMapper {

    /**
     * Maps a player entity to a response DTO.
     *
     * @param player entity to map
     * @return response DTO
     */
    public PlayerResponseDto toResponseDto(Player player) {
        return PlayerResponseDto.builder()
                .playerId(player.getPlayerId())
                .playerName(player.getPlayerName())
                .level(player.getLevel())
                .createdAt(player.getCreatedAt())
                .build();
    }

    /**
     * Updates an existing entity from a request DTO.
     *
     * @param entity existing entity
     * @param requestDto incoming data
     */
    public void updateEntity(Player entity, PlayerRequestDto requestDto) {
        entity.setPlayerName(requestDto.getPlayerName());
        entity.setLevel(requestDto.getLevel());
    }
}
