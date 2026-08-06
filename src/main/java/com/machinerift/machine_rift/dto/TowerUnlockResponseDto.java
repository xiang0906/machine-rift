package com.machinerift.machine_rift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Result returned after permanently unlocking a tower.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TowerUnlockResponseDto {

    private Long towerId;
    private String towerName;
    private Integer unlockCost;
    private Integer remainingGold;
    private LocalDateTime unlockedAt;
}
