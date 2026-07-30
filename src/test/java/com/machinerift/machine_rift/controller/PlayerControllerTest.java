package com.machinerift.machine_rift.controller;

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
        when(authService.requirePlayer(null, 1L))
                .thenThrow(new AuthenticationException("請先登入"));

        mockMvc.perform(get("/api/players/1/progress"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("請先登入"));
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
