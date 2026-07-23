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
 * Ordered grid waypoint belonging to a stage.
 */
@Entity
@Table(name = "stage_path")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagePath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stage_path_id")
    private Long stagePathId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Stage stage;

    @Column(name = "point_order", nullable = false)
    private Integer pointOrder;

    @Column(name = "grid_col", nullable = false)
    private Integer gridCol;

    @Column(name = "grid_row", nullable = false)
    private Integer gridRow;
}
