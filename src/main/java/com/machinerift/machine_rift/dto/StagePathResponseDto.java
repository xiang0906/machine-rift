package com.machinerift.machine_rift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ordered grid waypoint returned with a stage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagePathResponseDto {

    private Integer pointOrder;
    private Integer gridCol;
    private Integer gridRow;
}
