package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.Enemy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for enemy configuration persistence.
 */
@Repository
public interface EnemyRepository extends JpaRepository<Enemy, Long> {
}
