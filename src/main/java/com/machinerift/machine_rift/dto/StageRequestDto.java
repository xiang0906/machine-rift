package com.machinerift.machine_rift.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for creating or updating a stage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageRequestDto {

    @NotBlank(message = "Stage name is required")
    @Size(max = 100, message = "Stage name must be at most 100 characters")
    private String stageName;

    @NotBlank(message = "Difficulty is required")
    @Size(max = 50, message = "Difficulty must be at most 50 characters")
    private String difficulty;

    @Min(value = 0, message = "Reward gold must be non-negative")
    private Integer rewardGold;

    @Min(value = 1, message = "Enemy count must be at least 1")
    private Integer enemyCount;
}
