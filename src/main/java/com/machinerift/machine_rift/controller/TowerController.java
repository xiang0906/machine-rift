package com.machinerift.machine_rift.controller;

import com.machinerift.machine_rift.dto.ApiResponse;
import com.machinerift.machine_rift.dto.TowerResponseDto;
import com.machinerift.machine_rift.dto.TowerUnlockResponseDto;
import com.machinerift.machine_rift.entity.Player;
import com.machinerift.machine_rift.service.AuthService;
import com.machinerift.machine_rift.service.TowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for tower endpoints.
 */
@RestController
@RequestMapping("/api/towers")
@RequiredArgsConstructor
public class TowerController {

    private final TowerService towerService;
    private final AuthService authService;

    /**
     * Lists all towers.
     *
     * @return response entity containing a list of towers
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TowerResponseDto>>> getAllTowers() {
        return ResponseEntity.ok(ApiResponse.success("已取得防禦塔列表", towerService.getAllTowers()));
    }

    /**
     * Permanently unlocks a selected tower for the authenticated player.
     */
    @PostMapping("/{towerId}/unlock")
    public ResponseEntity<ApiResponse<TowerUnlockResponseDto>> unlockTower(
            @PathVariable Long towerId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Player player = authService.requirePlayer(authorization);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "防禦塔解鎖成功",
                        towerService.unlockTower(towerId, player)));
    }

}
