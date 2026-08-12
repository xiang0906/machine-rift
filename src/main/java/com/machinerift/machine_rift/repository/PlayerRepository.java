package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.Player;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository for player persistence operations.
 */
public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByUsername(String username);

    Optional<Player> findBySessionTokenHash(String sessionTokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select player from Player player where player.playerId = :playerId")
    Optional<Player> findByIdForUpdate(@Param("playerId") Long playerId);

    boolean existsByUsername(String username);

    boolean existsByPlayerNameIgnoreCase(String playerName);
}
