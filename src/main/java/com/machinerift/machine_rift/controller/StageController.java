package com.machinerift.machine_rift.controller;

import com.machinerift.machine_rift.dto.ApiResponse;
import com.machinerift.machine_rift.dto.StageRequestDto;
import com.machinerift.machine_rift.dto.StageResponseDto;
import com.machinerift.machine_rift.service.StageService;
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

    /**
     * Retrieves a single stage by id.
     *
     * @param id stage id
     * @return response entity containing the stage
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StageResponseDto>> getStageById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Stage retrieved successfully.", stageService.getStageById(id)));
    }

    /**
     * Creates a new stage.
     *
     * @param requestDto validated request payload
     * @return created stage response
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StageResponseDto>> createStage(@Valid @RequestBody StageRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stage created successfully.", stageService.createStage(requestDto)));
    }

    /**
     * Updates an existing stage.
     *
     * @param id stage id
     * @param requestDto validated request payload
     * @return updated stage response
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StageResponseDto>> updateStage(@PathVariable Long id,
                                                                      @Valid @RequestBody StageRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success("Stage updated successfully.", stageService.updateStage(id, requestDto)));
    }

    /**
     * Deletes a stage by id.
     *
     * @param id stage id
     * @return empty success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStage(@PathVariable Long id) {
        stageService.deleteStage(id);
        return ResponseEntity.ok(ApiResponse.success("Stage deleted successfully.", null));
    }
}
