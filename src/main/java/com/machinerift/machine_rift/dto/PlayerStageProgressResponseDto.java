package com.machinerift.machine_rift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unlock and personal-best details for one stage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStageProgressResponseDto {

    private Long stageId;
    private String stageName;
    private Boolean unlocked;
    private Integer bestScore;
    private Integer bestPlayTime;
    private Integer completionCount;
}
