package com.machinerift.machine_rift.controller;

import com.machinerift.machine_rift.dto.ApiResponse;
import com.machinerift.machine_rift.dto.StageResponseDto;
import com.machinerift.machine_rift.service.StageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for stage endpoints.
 */
@RestController
@RequestMapping("/api/stages")
@RequiredArgsConstructor
public class StageController {

    private final StageService stageService;

    /**
     * Lists all stages.
     *
     * @return response entity containing a list of stages
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StageResponseDto>>> getAllStages() {
        return ResponseEntity.ok(ApiResponse.success("Stages retrieved successfully.", stageService.getAllStages()));
    }

}
