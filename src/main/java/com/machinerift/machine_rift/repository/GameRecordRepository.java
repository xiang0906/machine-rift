package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.GameRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for game record persistence operations.
 */
@Repository
public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {

    /**
     * Finds game records ordered by score descending.
     *
     * @return ranking list
     */
    List<GameRecord> findAllByOrderByScoreDesc();

    /**
     * Checks whether a player is referenced by a saved game record.
     *
     * @param playerId player id
     * @return true when deleting the player would break game history
     */
    boolean existsByPlayerPlayerId(Long playerId);

    /**
     * Checks whether a stage is referenced by a saved game record.
     *
     * @param stageId stage id
     * @return true when deleting the stage would break game history
     */
    boolean existsByStageStageId(Long stageId);
}
