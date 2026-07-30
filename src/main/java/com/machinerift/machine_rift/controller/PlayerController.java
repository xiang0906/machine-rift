package com.machinerift.machine_rift.controller;

import com.machinerift.machine_rift.dto.ApiResponse;
import com.machinerift.machine_rift.dto.PlayerProgressResponseDto;
import com.machinerift.machine_rift.entity.Player;
import com.machinerift.machine_rift.service.PlayerProgressService;
import com.machinerift.machine_rift.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    @GetMapping("/me/progress")
    public ResponseEntity<ApiResponse<PlayerProgressResponseDto>> getPlayerProgress(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Player player = authService.requirePlayer(authorization);
        return ResponseEntity.ok(ApiResponse.success(
                "已取得玩家進度",
                playerProgressService.getProgress(player)));
    }

}
