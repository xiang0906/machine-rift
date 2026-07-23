package com.machinerift.machine_rift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Compact tower details included in a player's unlocked content.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnlockedTowerResponseDto {

    private Long towerId;
    private String towerName;
}
