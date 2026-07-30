package com.machinerift.machine_rift.controller;

import com.machinerift.machine_rift.dto.ApiResponse;
import com.machinerift.machine_rift.dto.PlayerProgressResponseDto;
import com.machinerift.machine_rift.service.PlayerProgressService;
import com.machinerift.machine_rift.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for player endpoints.
 */
@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerProgressService playerProgressService;
    private final AuthService authService;

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

}
