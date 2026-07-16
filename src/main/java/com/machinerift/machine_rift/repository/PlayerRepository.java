package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for player persistence operations.
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
}
