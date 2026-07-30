package com.machinerift.machine_rift.controller;

import com.machinerift.machine_rift.dto.RankingEntryResponseDto;
import com.machinerift.machine_rift.dto.RankingResponseDto;
import com.machinerift.machine_rift.service.AuthService;
import com.machinerift.machine_rift.service.GameRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameRecordController.class)
class GameRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameRecordService gameRecordService;

    @MockBean
    private AuthService authService;

    @Test
    void shouldReturnBackendAggregatedRanking() throws Exception {
        RankingEntryResponseDto entry = RankingEntryResponseDto.builder()
                .rank(1)
                .playerName("Alice")
                .stageName("核心裂谷")
                .score(1200)
                .playTime(50)
                .result("WIN")
                .build();
        when(gameRecordService.getRankings()).thenReturn(RankingResponseDto.builder()
                .participantCount(3)
                .totalGameCount(8)
                .entries(List.of(entry))
                .build());

        mockMvc.perform(get("/api/rankings")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.participantCount").value(3))
                .andExpect(jsonPath("$.data.totalGameCount").value(8))
                .andExpect(jsonPath("$.data.entries[0].rank").value(1))
                .andExpect(jsonPath("$.data.entries[0].playerName").value("Alice"))
                .andExpect(jsonPath("$.data.entries[0].stageName").value("核心裂谷"))
                .andExpect(jsonPath("$.data.entries[0].score").value(1200))
                .andExpect(jsonPath("$.data.entries[0].playTime").value(50))
                .andExpect(jsonPath("$.data.entries[0].result").value("WIN"));
    }

    @Test
    void shouldNotExposeUnexpectedRankingExceptionDetails() throws Exception {
        when(gameRecordService.getRankings())
                .thenThrow(new RuntimeException("Database connection secret"));

        mockMvc.perform(get("/api/rankings")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("系統發生未預期的錯誤，請稍後再試"))
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.not("Database connection secret")));
    }
}
