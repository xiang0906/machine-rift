package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.Tower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for tower persistence operations.
 */
@Repository
public interface TowerRepository extends JpaRepository<Tower, Long> {

    List<Tower> findAllByOrderByCostAscTowerIdAsc();
}
