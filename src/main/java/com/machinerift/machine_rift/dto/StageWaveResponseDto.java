package com.machinerift.machine_rift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wave configuration returned with a stage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageWaveResponseDto {

    private Integer waveNumber;
    private Integer enemyCount;
    private Integer spawnIntervalMs;
    private EnemyResponseDto enemy;
}
