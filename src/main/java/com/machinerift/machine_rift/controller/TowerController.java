package com.machinerift.machine_rift.controller;

import com.machinerift.machine_rift.dto.ApiResponse;
import com.machinerift.machine_rift.dto.TowerResponseDto;
import com.machinerift.machine_rift.service.TowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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

    /**
     * Lists all towers.
     *
     * @return response entity containing a list of towers
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TowerResponseDto>>> getAllTowers() {
        return ResponseEntity.ok(ApiResponse.success("Towers retrieved successfully.", towerService.getAllTowers()));
    }

    /**
     * Retrieves a single tower by id.
     *
     * @param id tower id
     * @return response entity containing the tower
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TowerResponseDto>> getTowerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Tower retrieved successfully.", towerService.getTowerById(id)));
    }
}
