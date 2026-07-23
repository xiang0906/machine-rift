package com.machinerift.machine_rift.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Aggregate progression values belonging to one player.
 */
@Entity
@Table(name = "player_progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerProgress {

    @Id
    @Column(name = "player_id")
    private Long playerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "player_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Player player;

    @Column(name = "experience", nullable = false)
    private Integer experience;

    @Column(name = "gold", nullable = false)
    private Integer gold;

    @Column(name = "completed_stages", nullable = false)
    private Integer completedStages;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
