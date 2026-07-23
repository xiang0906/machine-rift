package com.machinerift.machine_rift.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Per-stage unlock and personal-best state for one player.
 */
@Entity
@Table(name = "player_stage_progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStageProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "player_stage_progress_id")
    private Long playerStageProgressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Column(name = "unlocked", nullable = false)
    private Boolean unlocked;

    @Column(name = "best_score")
    private Integer bestScore;

    @Column(name = "best_play_time")
    private Integer bestPlayTime;

    @Column(name = "completion_count", nullable = false)
    private Integer completionCount;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
