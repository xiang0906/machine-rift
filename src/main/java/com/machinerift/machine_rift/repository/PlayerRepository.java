package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for player persistence operations.
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByPlayerNameIgnoreCase(String playerName);

    boolean existsByPlayerNameIgnoreCaseAndPlayerIdNot(String playerName, Long playerId);
}
