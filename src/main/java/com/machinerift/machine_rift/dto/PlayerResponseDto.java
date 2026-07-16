package com.machinerift.machine_rift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response payload returned to clients for player resources.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerResponseDto {

    private Long playerId;
    private String playerName;
    private Integer level;
    private LocalDateTime createdAt;
}
