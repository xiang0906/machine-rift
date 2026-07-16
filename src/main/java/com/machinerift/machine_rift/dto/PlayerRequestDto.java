package com.machinerift.machine_rift.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for creating or updating a player.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRequestDto {

    @NotBlank(message = "Player name is required")
    @Size(max = 100, message = "Player name must be at most 100 characters")
    private String playerName;

    @Min(value = 1, message = "Level must be at least 1")
    private Integer level;
}
