package com.machinerift.machine_rift.controller;

import com.machinerift.machine_rift.dto.ApiResponse;
import com.machinerift.machine_rift.dto.PlayerRequestDto;
import com.machinerift.machine_rift.dto.PlayerProgressResponseDto;
import com.machinerift.machine_rift.dto.PlayerResponseDto;
import com.machinerift.machine_rift.service.PlayerService;
import com.machinerift.machine_rift.service.PlayerProgressService;
import com.machinerift.machine_rift.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for player endpoints.
 */
@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;
    private final PlayerProgressService playerProgressService;
    private final AuthService authService;

    /**
     * Lists all players.
     *
     * @return response entity containing a list of players
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlayerResponseDto>>> getAllPlayers() {
        return ResponseEntity.ok(ApiResponse.success("Players retrieved successfully.", playerService.getAllPlayers()));
    }

    /**
     * Retrieves a single player by id.
     *
     * @param id player id
     * @return response entity containing the player
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlayerResponseDto>> getPlayerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Player retrieved successfully.", playerService.getPlayerById(id)));
    }

    /**
     * Returns progression, unlocked content, and personal bests.
     */
    @GetMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<PlayerProgressResponseDto>> getPlayerProgress(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requirePlayer(authorization, id);
        return ResponseEntity.ok(ApiResponse.success(
                "Player progress retrieved successfully.",
                playerProgressService.getProgress(id)));
    }

    /**
     * Updates an existing player.
     *
     * @param id player id
     * @param requestDto validated request payload
     * @return updated player response
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PlayerResponseDto>> updatePlayer(@PathVariable Long id,
                                                                      @Valid @RequestBody PlayerRequestDto requestDto,
                                                                      @RequestHeader(value = "Authorization", required = false)
                                                                      String authorization) {
        authService.requirePlayer(authorization, id);
        return ResponseEntity.ok(ApiResponse.success("Player updated successfully.", playerService.updatePlayer(id, requestDto)));
    }

    /**
     * Deletes a player by id.
     *
     * @param id player id
     * @return empty success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlayer(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requirePlayer(authorization, id);
        playerService.deletePlayer(id);
        return ResponseEntity.ok(ApiResponse.success("Player deleted successfully.", null));
    }
}
