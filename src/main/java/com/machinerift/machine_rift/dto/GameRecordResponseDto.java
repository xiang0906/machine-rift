package com.machinerift.machine_rift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload returned to clients for a game record.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRecordResponseDto {

    private Long recordId;
    private Long playerId;
    private Long stageId;
    private Integer score;
    private String result;
    private Integer playTime;
}
