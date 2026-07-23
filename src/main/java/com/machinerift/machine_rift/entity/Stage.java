package com.machinerift.machine_rift.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Persistent entity representing a game stage.
 */
@Entity
@Table(name = "stage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stage_id")
    private Long stageId;

    @Column(name = "stage_name", nullable = false, length = 100)
    private String stageName;

    @Column(name = "difficulty", nullable = false, length = 50)
    private String difficulty;

    @Column(name = "reward_gold", nullable = false)
    private Integer rewardGold;

    @Column(name = "enemy_count", nullable = false)
    private Integer enemyCount;

    @OneToMany(mappedBy = "stage")
    @OrderBy("pointOrder ASC")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<StagePath> path = new ArrayList<>();

    @OneToMany(mappedBy = "stage")
    @OrderBy("waveNumber ASC")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<StageWave> waves = new ArrayList<>();
}
