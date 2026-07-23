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

    @NotBlank(message = "請輸入玩家顯示名稱")
    @Size(max = 100, message = "玩家顯示名稱最多 100 個字元")
    private String playerName;

    @Min(value = 1, message = "玩家等級至少為 1")
    private Integer level;
}
