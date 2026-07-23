package com.machinerift.machine_rift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response payload returned to clients for stage resources.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageResponseDto {

    private Long stageId;
    private String stageName;
    private String difficulty;
    private Integer rewardGold;
    private Integer enemyCount;
    private List<StagePathResponseDto> path;
    private List<StageWaveResponseDto> waves;
}
