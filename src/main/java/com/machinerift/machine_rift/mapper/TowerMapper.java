package com.machinerift.machine_rift.mapper;

import com.machinerift.machine_rift.dto.TowerRequestDto;
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

    /**
     * Maps a request DTO to a new entity.
     *
     * @param requestDto incoming data
     * @return entity ready for persistence
     */
    public Tower toEntity(TowerRequestDto requestDto) {
        return Tower.builder()
                .towerName(requestDto.getTowerName())
                .towerType(requestDto.getTowerType())
                .damage(requestDto.getDamage())
                .attackSpeed(requestDto.getAttackSpeed())
                .attackRange(requestDto.getAttackRange())
                .cost(requestDto.getCost())
                .build();
    }

    /**
     * Updates an existing entity from a request DTO.
     *
     * @param entity existing entity
     * @param requestDto incoming data
     */
    public void updateEntity(Tower entity, TowerRequestDto requestDto) {
        entity.setTowerName(requestDto.getTowerName());
        entity.setTowerType(requestDto.getTowerType());
        entity.setDamage(requestDto.getDamage());
        entity.setAttackSpeed(requestDto.getAttackSpeed());
        entity.setAttackRange(requestDto.getAttackRange());
        entity.setCost(requestDto.getCost());
    }
}
