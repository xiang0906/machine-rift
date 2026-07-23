package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.StagePath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for ordered stage path points.
 */
@Repository
public interface StagePathRepository extends JpaRepository<StagePath, Long> {
}
