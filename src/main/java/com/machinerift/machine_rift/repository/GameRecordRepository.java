package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.GameRecord;
import com.machinerift.machine_rift.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for game record persistence operations.
 */
@Repository
public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {

    @EntityGraph(attributePaths = {"player", "stage"})
    List<GameRecord> findAllByOrderByScoreDescPlayTimeAscRecordIdAsc();

    @EntityGraph(attributePaths = {"stage"})
    List<GameRecord> findTop20ByPlayerOrderByCreatedAtDescRecordIdDesc(Player player);
}
