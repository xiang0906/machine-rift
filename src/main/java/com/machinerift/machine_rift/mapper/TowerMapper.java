package com.machinerift.machine_rift.mapper;

import com.machinerift.machine_rift.dto.TowerResponseDto;
import com.machinerift.machine_rift.entity.Tower;
import org.springframework.stereotype.Component;

/**
 * Converts between tower entities and DTOs.
 */
@Component
public class TowerMapper {

    /**
     * Maps a tower entity to a response DTO.
     *
     * @param tower entity to map
     * @return response DTO
     */
    public TowerResponseDto toResponseDto(Tower tower) {
        return TowerResponseDto.builder()
                .towerId(tower.getTowerId())
                .towerName(tower.getTowerName())
                .towerType(tower.getTowerType())
                .damage(tower.getDamage())
                .attackSpeed(tower.getAttackSpeed())
                .attackRange(tower.getAttackRange())
                .cost(tower.getCost())
                .build();
    }
}
