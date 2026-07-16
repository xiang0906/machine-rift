package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for stage persistence operations.
 */
@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {
}
