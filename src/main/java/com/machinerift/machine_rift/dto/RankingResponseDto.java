package com.machinerift.machine_rift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Leaderboard summary and its top-ranked entries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingResponseDto {

    private Integer participantCount;
    private Integer totalGameCount;
    private List<RankingEntryResponseDto> entries;
}
