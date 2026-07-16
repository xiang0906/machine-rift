package com.machinerift.machine_rift.mapper;

import com.machinerift.machine_rift.dto.GameRecordRequestDto;
import com.machinerift.machine_rift.dto.GameRecordResponseDto;
import com.machinerift.machine_rift.entity.GameRecord;
import com.machinerift.machine_rift.entity.Player;
import com.machinerift.machine_rift.entity.Stage;
import org.springframework.stereotype.Component;

/**
 * Converts between game record entities and DTOs.
 */
@Component
public class GameRecordMapper {

    /**
     * Maps a game record entity to a response DTO.
     *
     * @param gameRecord entity to map
     * @return response DTO
     */
    public GameRecordResponseDto toResponseDto(GameRecord gameRecord) {
        return GameRecordResponseDto.builder()
                .recordId(gameRecord.getRecordId())
                .playerId(gameRecord.getPlayer().getPlayerId())
                .stageId(gameRecord.getStage().getStageId())
                .score(gameRecord.getScore())
                .result(gameRecord.getResult())
                .playTime(gameRecord.getPlayTime())
                .build();
    }

    /**
     * Maps a request DTO to a new entity.
     *
     * @param requestDto incoming data
     * @param player related player entity
     * @param stage related stage entity
     * @return entity ready for persistence
     */
    public GameRecord toEntity(GameRecordRequestDto requestDto, Player player, Stage stage) {
        return GameRecord.builder()
                .player(player)
                .stage(stage)
                .score(requestDto.getScore())
                .result(requestDto.getResult())
                .playTime(requestDto.getPlayTime())
                .build();
    }
}
