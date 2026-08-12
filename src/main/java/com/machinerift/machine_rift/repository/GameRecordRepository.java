package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.GameRecord;
import com.machinerift.machine_rift.entity.Player;
import com.machinerift.machine_rift.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

/**
 * Repository for game record persistence operations.
 */
public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {

    @EntityGraph(attributePaths = {"player", "stage"})
    List<GameRecord> findAllByOrderByScoreDescPlayTimeAscRecordIdAsc();

    @EntityGraph(attributePaths = {"player", "stage"})
    List<GameRecord> findAllByStageOrderByScoreDescPlayTimeAscRecordIdAsc(Stage stage);

    @EntityGraph(attributePaths = {"stage"})
    List<GameRecord> findTop20ByPlayerOrderByCreatedAtDescRecordIdDesc(Player player);
}
