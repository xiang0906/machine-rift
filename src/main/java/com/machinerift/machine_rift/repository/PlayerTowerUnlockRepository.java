package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.PlayerTowerUnlock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for a player's permanently unlocked towers.
 */
public interface PlayerTowerUnlockRepository extends JpaRepository<PlayerTowerUnlock, Long> {

    @EntityGraph(attributePaths = {"tower"})
    List<PlayerTowerUnlock> findAllByPlayerPlayerIdOrderByTowerCostAscTowerTowerIdAsc(Long playerId);

    boolean existsByPlayerPlayerIdAndTowerTowerId(Long playerId, Long towerId);
}
