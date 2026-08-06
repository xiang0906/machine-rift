package com.machinerift.machine_rift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * One completed game shown in the authenticated player's history.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRecordHistoryResponseDto {

    private Long recordId;
    private Long stageId;
    private String stageName;
    private Integer score;
    private String result;
    private Integer playTime;
    private LocalDateTime createdAt;
}
