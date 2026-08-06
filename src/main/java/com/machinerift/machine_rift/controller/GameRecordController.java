package com.machinerift.machine_rift.controller;

import com.machinerift.machine_rift.dto.ApiResponse;
import com.machinerift.machine_rift.dto.GameRecordRequestDto;
import com.machinerift.machine_rift.dto.GameRecordHistoryResponseDto;
import com.machinerift.machine_rift.dto.GameRecordResponseDto;
import com.machinerift.machine_rift.dto.RankingResponseDto;
import com.machinerift.machine_rift.entity.Player;
import com.machinerift.machine_rift.service.GameRecordService;
import com.machinerift.machine_rift.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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
    private final AuthService authService;

    /**
     * Saves a finished game session.
     *
     * @param requestDto validated game record payload
     * @return created game record response
     */
    @PostMapping("/game-records")
    public ResponseEntity<ApiResponse<GameRecordResponseDto>> saveGameRecord(
            @Valid @RequestBody GameRecordRequestDto requestDto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Player player = authService.requirePlayer(authorization);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "戰績儲存成功",
                        gameRecordService.saveGameRecord(requestDto, player)));
    }

    /**
     * Retrieves the authenticated player's latest game records.
     */
    @GetMapping("/game-records/me")
    public ResponseEntity<ApiResponse<List<GameRecordHistoryResponseDto>>> getMyGameRecords(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Player player = authService.requirePlayer(authorization);
        return ResponseEntity.ok(ApiResponse.success(
                "已取得個人歷史戰績",
                gameRecordService.getPlayerHistory(player)));
    }

    /**
     * Retrieves top-ranked game records.
     *
     * @return response entity containing ranking data
     */
    @GetMapping("/rankings")
    public ResponseEntity<ApiResponse<RankingResponseDto>> getRankings(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requirePlayer(authorization);
        return ResponseEntity.ok(ApiResponse.success("已取得排行榜", gameRecordService.getRankings()));
    }
}
