package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.Enemy;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for enemy configuration persistence.
 */
public interface EnemyRepository extends JpaRepository<Enemy, Long> {
}
