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

/**
 * Enemy and spawning configuration for one stage wave.
 */
@Entity
@Table(name = "stage_wave")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageWave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stage_wave_id")
    private Long stageWaveId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Stage stage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enemy_id", nullable = false)
    private Enemy enemy;

    @Column(name = "wave_number", nullable = false)
    private Integer waveNumber;

    @Column(name = "enemy_count", nullable = false)
    private Integer enemyCount;

    @Column(name = "spawn_interval_ms", nullable = false)
    private Integer spawnIntervalMs;
}
