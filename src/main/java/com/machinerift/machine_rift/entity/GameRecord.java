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
import lombok.NoArgsConstructor;

/**
 * Persistent entity representing a completed game session.
 */
@Entity
@Table(name = "game_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "result", nullable = false, length = 20)
    private String result;

    @Column(name = "play_time", nullable = false)
    private Integer playTime;
}
