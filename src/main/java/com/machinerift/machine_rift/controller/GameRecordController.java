package com.machinerift.machine_rift.controller;

import com.machinerift.machine_rift.dto.ApiResponse;
import com.machinerift.machine_rift.dto.GameRecordRequestDto;
import com.machinerift.machine_rift.dto.GameRecordResponseDto;
import com.machinerift.machine_rift.service.GameRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for game record endpoints and rankings.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GameRecordController {

    private final GameRecordService gameRecordService;

    /**
     * Saves a finished game session.
     *
     * @param requestDto validated game record payload
     * @return created game record response
     */
    @PostMapping("/game-records")
    public ResponseEntity<ApiResponse<GameRecordResponseDto>> saveGameRecord(@Valid @RequestBody GameRecordRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Game record saved successfully.", gameRecordService.saveGameRecord(requestDto)));
    }

    /**
     * Lists all game records.
     *
     * @return response entity containing game record history
     */
    @GetMapping("/game-records")
    public ResponseEntity<ApiResponse<List<GameRecordResponseDto>>> getAllGameRecords() {
        return ResponseEntity.ok(ApiResponse.success("Game records retrieved successfully.", gameRecordService.getAllGameRecords()));
    }

    /**
     * Retrieves a single game record by id.
     *
     * @param id record id
     * @return response entity containing the requested game record
     */
    @GetMapping("/game-records/{id}")
    public ResponseEntity<ApiResponse<GameRecordResponseDto>> getGameRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Game record retrieved successfully.", gameRecordService.getGameRecordById(id)));
    }

    /**
     * Retrieves top-ranked game records.
     *
     * @return response entity containing ranking data
     */
    @GetMapping("/rankings")
    public ResponseEntity<ApiResponse<List<GameRecordResponseDto>>> getRankings() {
        return ResponseEntity.ok(ApiResponse.success("Ranking retrieved successfully.", gameRecordService.getRankings()));
    }
}
