package com.machinerift.machine_rift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload returned to clients for tower resources.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TowerResponseDto {

    private Long towerId;
    private String towerName;
    private String towerType;
    private Integer damage;
    private Double attackSpeed;
    private Integer attackRange;
    private Integer cost;
}
