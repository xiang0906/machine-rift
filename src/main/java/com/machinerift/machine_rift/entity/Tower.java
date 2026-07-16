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
 * Persistent entity representing a tower available in the game.
 */
@Entity
@Table(name = "tower")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tower_id")
    private Long towerId;

    @Column(name = "tower_name", nullable = false, length = 100)
    private String towerName;

    @Column(name = "tower_type", nullable = false, length = 50)
    private String towerType;

    @Column(name = "damage", nullable = false)
    private Integer damage;

    @Column(name = "attack_speed", nullable = false)
    private Double attackSpeed;

    @Column(name = "attack_range", nullable = false)
    private Integer attackRange;

    @Column(name = "cost", nullable = false)
    private Integer cost;
}
