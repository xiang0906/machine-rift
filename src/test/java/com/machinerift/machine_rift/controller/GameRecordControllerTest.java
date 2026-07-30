package com.machinerift.machine_rift.controller;

import com.machinerift.machine_rift.dto.GameRecordResponseDto;
import com.machinerift.machine_rift.dto.RankingEntryResponseDto;
import com.machinerift.machine_rift.dto.RankingResponseDto;
import com.machinerift.machine_rift.entity.Player;
import com.machinerift.machine_rift.service.AuthService;
import com.machinerift.machine_rift.service.GameRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest(GameRecordController.class)
class GameRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameRecordService gameRecordService;

    @MockBean
    private AuthService authService;

    @Test
    void shouldSaveRecordForPlayerResolvedFromToken() throws Exception {
        Player authenticatedPlayer = Player.builder()
                .playerId(7L)
                .playerName("Alice")
                .build();
        when(authService.requirePlayer("Bearer test-token"))
                .thenReturn(authenticatedPlayer);
        when(gameRecordService.saveGameRecord(any(), same(authenticatedPlayer)))
                .thenReturn(GameRecordResponseDto.builder()
                        .recordId(11L)
                        .playerId(7L)
                        .stageId(2L)
                        .score(900)
                        .result("WIN")
                        .playTime(80)
                        .build());

        mockMvc.perform(post("/api/game-records")
                        .header("Authorization", "Bearer test-token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "stageId": 2,
                                  "score": 900,
                                  "result": "WIN",
                                  "playTime": 80
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.playerId").value(7))
                .andExpect(jsonPath("$.data.stageId").value(2));
    }

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
