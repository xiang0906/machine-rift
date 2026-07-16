package com.machinerift.machine_rift.controller;

import com.machinerift.machine_rift.dto.ApiResponse;
import com.machinerift.machine_rift.dto.PlayerRequestDto;
import com.machinerift.machine_rift.dto.PlayerResponseDto;
import com.machinerift.machine_rift.service.PlayerService;
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
 * REST controller for player endpoints.
 */
@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

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
     * Creates a new player.
     *
     * @param requestDto validated request payload
     * @return created player response
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PlayerResponseDto>> createPlayer(@Valid @RequestBody PlayerRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Player created successfully.", playerService.createPlayer(requestDto)));
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
                                                                      @Valid @RequestBody PlayerRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success("Player updated successfully.", playerService.updatePlayer(id, requestDto)));
    }

    /**
     * Deletes a player by id.
     *
     * @param id player id
     * @return empty success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
        return ResponseEntity.ok(ApiResponse.success("Player deleted successfully.", null));
    }
}
