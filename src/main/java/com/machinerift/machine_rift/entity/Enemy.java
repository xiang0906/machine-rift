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
 * Persistent enemy configuration used by stage waves.
 */
@Entity
@Table(name = "enemy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enemy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enemy_id")
    private Long enemyId;

    @Column(name = "enemy_name", nullable = false, unique = true, length = 100)
    private String enemyName;

    @Column(name = "health", nullable = false)
    private Integer health;

    @Column(name = "speed", nullable = false)
    private Double speed;

    @Column(name = "reward_gold", nullable = false)
    private Integer rewardGold;
}
