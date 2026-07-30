package com.machinerift.machine_rift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One player's best game record in the leaderboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingEntryResponseDto {

    private Integer rank;
    private String playerName;
    private String stageName;
    private Integer score;
    private Integer playTime;
    private String result;
}
