package com.machinerift.machine_rift.mapper;

import com.machinerift.machine_rift.dto.EnemyResponseDto;
import com.machinerift.machine_rift.dto.StagePathResponseDto;
import com.machinerift.machine_rift.dto.StageRequestDto;
import com.machinerift.machine_rift.dto.StageResponseDto;
import com.machinerift.machine_rift.dto.StageWaveResponseDto;
import com.machinerift.machine_rift.entity.Stage;
import org.springframework.stereotype.Component;

/**
 * Converts between stage entities and DTOs.
 */
@Component
public class StageMapper {

    /**
     * Maps a stage entity to a response DTO.
     *
     * @param stage entity to map
     * @return response DTO
     */
    public StageResponseDto toResponseDto(Stage stage) {
        return StageResponseDto.builder()
                .stageId(stage.getStageId())
                .stageName(stage.getStageName())
                .difficulty(stage.getDifficulty())
                .rewardGold(stage.getRewardGold())
                .enemyCount(stage.getEnemyCount())
                .path(stage.getPath().stream()
                        .map(point -> StagePathResponseDto.builder()
                                .pointOrder(point.getPointOrder())
                                .gridCol(point.getGridCol())
                                .gridRow(point.getGridRow())
                                .build())
                        .toList())
                .waves(stage.getWaves().stream()
                        .map(wave -> StageWaveResponseDto.builder()
                                .waveNumber(wave.getWaveNumber())
                                .enemyCount(wave.getEnemyCount())
                                .spawnIntervalMs(wave.getSpawnIntervalMs())
                                .enemy(EnemyResponseDto.builder()
                                        .enemyId(wave.getEnemy().getEnemyId())
                                        .enemyName(wave.getEnemy().getEnemyName())
                                        .health(wave.getEnemy().getHealth())
                                        .speed(wave.getEnemy().getSpeed())
                                        .rewardGold(wave.getEnemy().getRewardGold())
                                        .build())
                                .build())
                        .toList())
                .build();
    }

    /**
     * Maps a request DTO to a new entity.
     *
     * @param requestDto incoming data
     * @return entity ready for persistence
     */
    public Stage toEntity(StageRequestDto requestDto) {
        return Stage.builder()
                .stageName(requestDto.getStageName())
                .difficulty(requestDto.getDifficulty())
                .rewardGold(requestDto.getRewardGold())
                .enemyCount(requestDto.getEnemyCount())
                .build();
    }

    /**
     * Updates an existing entity from a request DTO.
     *
     * @param entity existing entity
     * @param requestDto incoming data
     */
    public void updateEntity(Stage entity, StageRequestDto requestDto) {
        entity.setStageName(requestDto.getStageName());
        entity.setDifficulty(requestDto.getDifficulty());
        entity.setRewardGold(requestDto.getRewardGold());
        entity.setEnemyCount(requestDto.getEnemyCount());
    }
}
