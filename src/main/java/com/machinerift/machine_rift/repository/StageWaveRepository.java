package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.StageWave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for stage wave configuration.
 */
@Repository
public interface StageWaveRepository extends JpaRepository<StageWave, Long> {
}
