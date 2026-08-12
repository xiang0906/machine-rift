package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

/**
 * Repository for stage persistence operations.
 */
public interface StageRepository extends JpaRepository<Stage, Long> {

    Optional<Stage> findFirstByOrderByStageIdAsc();

    Optional<Stage> findFirstByStageIdGreaterThanOrderByStageIdAsc(Long stageId);

    List<Stage> findAllByOrderByStageIdAsc();
}
