package com.machinerift.machine_rift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enemy configuration embedded in a stage wave response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnemyResponseDto {

    private Long enemyId;
    private String enemyName;
    private Integer health;
    private Double speed;
    private Integer rewardGold;
}
