package com.machinerift.machine_rift.repository;

import com.machinerift.machine_rift.entity.PlayerSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PlayerSessionRepository extends JpaRepository<PlayerSession, Long> {

    @EntityGraph(attributePaths = "player")
    Optional<PlayerSession> findByTokenHash(String tokenHash);

    void deleteByTokenHash(String tokenHash);

    void deleteAllByPlayerPlayerId(Long playerId);

    void deleteAllByExpiresAtBefore(LocalDateTime expiresAt);
}
