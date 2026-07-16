package com.machinerift.machine_rift.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.machinerift.machine_rift.dto.PlayerResponseDto;
import com.machinerift.machine_rift.exception.ResourceConflictException;
import com.machinerift.machine_rift.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerService playerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnPlayersWithApiResponseWrapper() throws Exception {
        PlayerResponseDto player = PlayerResponseDto.builder()
                .playerId(1L)
                .playerName("Alice")
                .level(5)
                .createdAt(LocalDateTime.of(2026, 7, 2, 10, 0))
                .build();

        when(playerService.getAllPlayers()).thenReturn(List.of(player));

        mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].playerName").value("Alice"));
    }

    @Test
    void shouldReturnFieldValidationErrors() throws Exception {
        mockMvc.perform(post("/api/players")
                        .contentType("application/json")
                        .content("""
                                {"playerName":"","level":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.playerName").value("Player name is required"))
                .andExpect(jsonPath("$.data.level").value("Level must be at least 1"));
    }

    @Test
    void shouldNotExposeUnexpectedExceptionDetails() throws Exception {
        when(playerService.getAllPlayers()).thenThrow(new RuntimeException("Database connection secret"));

        mockMvc.perform(get("/api/players"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred."))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not("Database connection secret")));
    }

    @Test
    void shouldReturnConflictWhenDeletingPlayerWithGameRecords() throws Exception {
        doThrow(new ResourceConflictException("Cannot delete player with existing game records."))
                .when(playerService).deletePlayer(1L);

        mockMvc.perform(delete("/api/players/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cannot delete player with existing game records."));
    }
}
