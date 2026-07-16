package com.machinerift.machine_rift.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for saving a game record.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRecordRequestDto {

    @NotNull(message = "Player id is required")
    private Long playerId;

    @NotNull(message = "Stage id is required")
    private Long stageId;

    @NotNull(message = "Score is required")
    @Min(value = 0, message = "Score must be non-negative")
    private Integer score;

    @NotBlank(message = "Result is required")
    private String result;

    @NotNull(message = "Play time is required")
    @Min(value = 0, message = "Play time must be non-negative")
    private Integer playTime;
}
