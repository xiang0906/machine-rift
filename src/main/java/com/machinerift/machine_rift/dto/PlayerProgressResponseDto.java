package com.machinerift.machine_rift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aggregate player progress, unlocked content, and per-stage personal bests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerProgressResponseDto {

    private Long playerId;
    private Integer level;
    private Integer experience;
    private Integer gold;
    private Integer completedStages;
    private List<PlayerStageProgressResponseDto> stages;
    private List<UnlockedTowerResponseDto> unlockedTowers;
}
