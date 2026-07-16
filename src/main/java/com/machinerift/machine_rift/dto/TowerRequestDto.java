package com.machinerift.machine_rift.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for creating or updating a tower.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TowerRequestDto {

    @NotBlank(message = "Tower name is required")
    @Size(max = 100, message = "Tower name must be at most 100 characters")
    private String towerName;

    @NotBlank(message = "Tower type is required")
    @Size(max = 50, message = "Tower type must be at most 50 characters")
    private String towerType;

    @NotNull(message = "Damage is required")
    @Min(value = 0, message = "Damage must be non-negative")
    private Integer damage;

    @NotNull(message = "Attack speed is required")
    @Min(value = 0, message = "Attack speed must be non-negative")
    private Double attackSpeed;

    @NotNull(message = "Attack range is required")
    @Min(value = 0, message = "Attack range must be non-negative")
    private Integer attackRange;

    @NotNull(message = "Cost is required")
    @Min(value = 0, message = "Cost must be non-negative")
    private Integer cost;
}
