package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.Tower;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for tower persistence operations.
 */
public interface TowerRepository extends JpaRepository<Tower, Long> {

    List<Tower> findAllByOrderByCostAscTowerIdAsc();
}
