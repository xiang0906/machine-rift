package com.machinerift.machine_rift.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Persistent entity representing a game player.
 */
@Entity
@Table(name = "player")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "player_id")
    private Long playerId;

    @Column(name = "player_name", nullable = false, length = 100)
    private String playerName;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
