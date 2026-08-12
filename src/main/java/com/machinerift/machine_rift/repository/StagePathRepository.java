package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.StagePath;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for ordered stage path points.
 */
public interface StagePathRepository extends JpaRepository<StagePath, Long> {
}
