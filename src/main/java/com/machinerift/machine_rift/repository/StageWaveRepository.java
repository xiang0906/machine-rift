package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.StageWave;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for stage wave configuration.
 */
public interface StageWaveRepository extends JpaRepository<StageWave, Long> {
}
