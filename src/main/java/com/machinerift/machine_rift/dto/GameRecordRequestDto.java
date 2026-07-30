package com.machinerift.machine_rift.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    @NotNull(message = "請選擇關卡")
    private Long stageId;

    @NotNull(message = "請提供分數")
    @Min(value = 0, message = "分數不可小於 0")
    private Integer score;

    @NotBlank(message = "請提供遊戲結果")
    @Pattern(regexp = "WIN|LOSE", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "遊戲結果只能是 WIN 或 LOSE")
    private String result;

    @NotNull(message = "請提供遊戲時間")
    @Min(value = 0, message = "遊戲時間不可小於 0")
    private Integer playTime;
}
