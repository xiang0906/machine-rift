package com.machinerift.machine_rift.controller;

import com.machinerift.machine_rift.dto.PlayerProgressResponseDto;
import com.machinerift.machine_rift.entity.Player;
import com.machinerift.machine_rift.exception.AuthenticationException;
import com.machinerift.machine_rift.service.PlayerProgressService;
import com.machinerift.machine_rift.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerProgressService playerProgressService;

    @MockBean
    private AuthService authService;

    @Test
    void shouldReturnNotFoundForRemovedPlayerListApi() throws Exception {
        mockMvc.perform(get("/api/players"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("找不到指定的 API 或資源"));
    }

    @Test
    void shouldRequireLoginForPlayerProgress() throws Exception {
        when(authService.requirePlayer(null))
                .thenThrow(new AuthenticationException("請先登入"));

        mockMvc.perform(get("/api/players/me/progress"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("請先登入"));
    }

    @Test
    void shouldReturnProgressForPlayerResolvedFromToken() throws Exception {
        Player authenticatedPlayer = Player.builder()
                .playerId(5L)
                .playerName("Alice")
                .build();
        when(authService.requirePlayer("Bearer test-token"))
                .thenReturn(authenticatedPlayer);
        when(playerProgressService.getProgress(authenticatedPlayer))
                .thenReturn(PlayerProgressResponseDto.builder()
                        .playerId(5L)
                        .level(3)
                        .experience(2200)
                        .build());

        mockMvc.perform(get("/api/players/me/progress")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("已取得玩家進度"))
                .andExpect(jsonPath("$.data.playerId").value(5))
                .andExpect(jsonPath("$.data.level").value(3));
    }

    @Test
    void shouldReturnNotFoundForRemovedPlayerIdProgressApi() throws Exception {
        mockMvc.perform(get("/api/players/1/progress")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("找不到指定的 API 或資源"));
    }

    @Test
    void shouldReturnNotFoundForRemovedPlayerDeleteApi() throws Exception {
        mockMvc.perform(delete("/api/players/1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("找不到指定的 API 或資源"));
    }

}
