package com.machinerift.machine_rift.controller;

import com.machinerift.machine_rift.dto.GameRecordHistoryResponseDto;
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
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
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
                .andExpect(jsonPath("$.message").value("戰績儲存成功"))
                .andExpect(jsonPath("$.data.playerId").value(7))
                .andExpect(jsonPath("$.data.stageId").value(2));
    }

    @Test
    void shouldReturnChineseGameRecordValidationMessages() throws Exception {
        mockMvc.perform(post("/api/game-records")
                        .header("Authorization", "Bearer test-token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "score": -1,
                                  "result": "DRAW",
                                  "playTime": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("輸入資料有誤"))
                .andExpect(jsonPath("$.data.stageId").value("請選擇關卡"))
                .andExpect(jsonPath("$.data.score").value("分數不可小於 0"))
                .andExpect(jsonPath("$.data.result").value("遊戲結果只能是 WIN 或 LOSE"))
                .andExpect(jsonPath("$.data.playTime").value("遊戲時間不可小於 0"));
    }

    @Test
    void shouldReturnAuthenticatedPlayersGameHistory() throws Exception {
        Player authenticatedPlayer = Player.builder()
                .playerId(7L)
                .playerName("Alice")
                .build();
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 6, 14, 30);
        when(authService.requirePlayer("Bearer test-token"))
                .thenReturn(authenticatedPlayer);
        when(gameRecordService.getPlayerHistory(same(authenticatedPlayer)))
                .thenReturn(List.of(GameRecordHistoryResponseDto.builder()
                        .recordId(11L)
                        .stageId(2L)
                        .stageName("核心裂谷")
                        .score(900)
                        .result("WIN")
                        .playTime(80)
                        .createdAt(createdAt)
                        .build()));

        mockMvc.perform(get("/api/game-records/me")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("已取得個人歷史戰績"))
                .andExpect(jsonPath("$.data[0].stageName").value("核心裂谷"))
                .andExpect(jsonPath("$.data[0].score").value(900))
                .andExpect(jsonPath("$.data[0].createdAt").value("2026-08-06T14:30:00"));
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
        when(gameRecordService.getRankings(null)).thenReturn(RankingResponseDto.builder()
                .participantCount(3)
                .totalGameCount(8)
                .entries(List.of(entry))
                .build());

        mockMvc.perform(get("/api/rankings")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("已取得排行榜"))
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
    void shouldForwardStageFilterToRankingService() throws Exception {
        when(gameRecordService.getRankings(3L)).thenReturn(RankingResponseDto.builder()
                .participantCount(0)
                .totalGameCount(0)
                .entries(List.of())
                .build());

        mockMvc.perform(get("/api/rankings")
                        .param("stageId", "3")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("已取得排行榜"))
                .andExpect(jsonPath("$.data.entries").isEmpty());

        verify(gameRecordService).getRankings(3L);
    }

    @Test
    void shouldNotExposeUnexpectedRankingExceptionDetails() throws Exception {
        when(gameRecordService.getRankings(null))
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
