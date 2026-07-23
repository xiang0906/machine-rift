package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository for stage persistence operations.
 */
@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {

    Optional<Stage> findFirstByOrderByStageIdAsc();

    Optional<Stage> findFirstByStageIdGreaterThanOrderByStageIdAsc(Long stageId);

    List<Stage> findAllByOrderByStageIdAsc();
}
