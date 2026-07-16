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
}
