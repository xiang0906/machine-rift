package com.machinerift.machine_rift.controller;

import com.machinerift.machine_rift.dto.ApiResponse;
import com.machinerift.machine_rift.dto.TowerRequestDto;
import com.machinerift.machine_rift.dto.TowerResponseDto;
import com.machinerift.machine_rift.service.TowerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * Creates a new tower.
     *
     * @param requestDto validated request payload
     * @return created tower response
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TowerResponseDto>> createTower(@Valid @RequestBody TowerRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tower created successfully.", towerService.createTower(requestDto)));
    }

    /**
     * Updates an existing tower.
     *
     * @param id tower id
     * @param requestDto validated request payload
     * @return updated tower response
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TowerResponseDto>> updateTower(@PathVariable Long id,
                                                                      @Valid @RequestBody TowerRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success("Tower updated successfully.", towerService.updateTower(id, requestDto)));
    }

    /**
     * Deletes a tower by id.
     *
     * @param id tower id
     * @return empty success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTower(@PathVariable Long id) {
        towerService.deleteTower(id);
        return ResponseEntity.ok(ApiResponse.success("Tower deleted successfully.", null));
    }
}
