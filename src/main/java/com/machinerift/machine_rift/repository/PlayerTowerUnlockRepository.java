package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.PlayerTowerUnlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerTowerUnlockRepository extends JpaRepository<PlayerTowerUnlock, Long> {

    List<PlayerTowerUnlock> findAllByPlayerPlayerIdOrderByTowerCostAscTowerTowerIdAsc(Long playerId);

    boolean existsByPlayerPlayerIdAndTowerTowerId(Long playerId, Long towerId);
}
