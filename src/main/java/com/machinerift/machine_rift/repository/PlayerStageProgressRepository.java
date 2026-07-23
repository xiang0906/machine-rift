package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.PlayerStageProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerStageProgressRepository extends JpaRepository<PlayerStageProgress, Long> {

    List<PlayerStageProgress> findAllByPlayerPlayerIdOrderByStageStageIdAsc(Long playerId);

    Optional<PlayerStageProgress> findByPlayerPlayerIdAndStageStageId(Long playerId, Long stageId);

    boolean existsByPlayerPlayerIdAndStageStageIdAndUnlockedTrue(Long playerId, Long stageId);
}
