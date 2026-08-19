package com.machinerift.machine_rift.service;

import com.machinerift.machine_rift.dto.PlayerProgressResponseDto;
import com.machinerift.machine_rift.dto.PlayerStageProgressResponseDto;
import com.machinerift.machine_rift.dto.UnlockedTowerResponseDto;
import com.machinerift.machine_rift.entity.Player;
import com.machinerift.machine_rift.entity.PlayerStageProgress;
import com.machinerift.machine_rift.entity.PlayerTowerUnlock;
import com.machinerift.machine_rift.entity.Stage;
import com.machinerift.machine_rift.entity.Tower;
import com.machinerift.machine_rift.repository.PlayerRepository;
import com.machinerift.machine_rift.repository.PlayerStageProgressRepository;
import com.machinerift.machine_rift.repository.PlayerTowerUnlockRepository;
import com.machinerift.machine_rift.repository.StageRepository;
import com.machinerift.machine_rift.repository.TowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Owns player progression, unlock rules, and per-stage personal bests.
 */
@Service
@RequiredArgsConstructor
public class PlayerProgressService {

    private final PlayerRepository playerRepository;
    private final PlayerStageProgressRepository playerStageProgressRepository;
    private final PlayerTowerUnlockRepository playerTowerUnlockRepository;
    private final StageRepository stageRepository;
    private final TowerRepository towerRepository;

    /**
     * Creates the progression aggregate and starter unlocks for a player.
     *
     * @param player persisted player
     */
    @Transactional
    public void initializePlayer(Player player) {
        LocalDateTime now = LocalDateTime.now();
        boolean playerChanged = false;
        if (player.getGold() == null) {
            player.setGold(0);
            playerChanged = true;
        }
        if (player.getCompletedStages() == null) {
            player.setCompletedStages(0);
            playerChanged = true;
        }

        findFirstPlayableStage().ifPresent(stage -> {
            if (!playerStageProgressRepository
                    .existsByPlayerPlayerIdAndStageStageIdAndUnlockedTrue(player.getPlayerId(), stage.getStageId())) {
                unlockStage(player, stage, now);
            }
        });

        towerRepository.findAllByOrderByCostAscTowerIdAsc().stream().findFirst().ifPresent(tower -> {
            if (!playerTowerUnlockRepository
                    .existsByPlayerPlayerIdAndTowerTowerId(player.getPlayerId(), tower.getTowerId())) {
                unlockTower(player, tower, now);
            }
        });
        if (player.getUpdatedAt() == null) {
            playerChanged = true;
        }
        if (playerChanged) {
            player.setUpdatedAt(now);
            playerRepository.save(player);
        }
    }

    /**
     * Returns the full progression view, including locked stages.
     *
     * @param player authenticated player
     * @return progression response
     */
    @Transactional
    public PlayerProgressResponseDto getProgress(Player player) {
        Long playerId = player.getPlayerId();
        initializePlayer(player);

        Map<Long, PlayerStageProgress> progressByStage = playerStageProgressRepository
                .findAllByPlayerPlayerIdOrderByStageStageIdAsc(playerId).stream()
                .collect(Collectors.toMap(
                        stageProgress -> stageProgress.getStage().getStageId(),
                        Function.identity()));

        var stages = stageRepository.findAllByOrderByStageIdAsc().stream()
                .map(stage -> {
                    PlayerStageProgress stageProgress = progressByStage.get(stage.getStageId());
                    return PlayerStageProgressResponseDto.builder()
                            .stageId(stage.getStageId())
                            .stageName(stage.getStageName())
                            .unlocked(stageProgress != null && Boolean.TRUE.equals(stageProgress.getUnlocked()))
                            .bestScore(stageProgress == null ? null : stageProgress.getBestScore())
                            .bestPlayTime(stageProgress == null ? null : stageProgress.getBestPlayTime())
                            .completionCount(stageProgress == null ? 0 : stageProgress.getCompletionCount())
                            .build();
                })
                .toList();

        var towers = playerTowerUnlockRepository
                .findAllByPlayerPlayerIdOrderByTowerCostAscTowerTowerIdAsc(playerId).stream()
                .map(unlock -> UnlockedTowerResponseDto.builder()
                        .towerId(unlock.getTower().getTowerId())
                        .towerName(unlock.getTower().getTowerName())
                        .build())
                .toList();

        return PlayerProgressResponseDto.builder()
                .playerId(playerId)
                .gold(player.getGold())
                .completedStages(player.getCompletedStages())
                .stages(stages)
                .unlockedTowers(towers)
                .build();
    }

    /**
     * Checks whether a player may submit a record for a stage.
     */
    @Transactional
    public boolean isStageUnlocked(Player player, Stage stage) {
        initializePlayer(player);
        return playerStageProgressRepository
                .existsByPlayerPlayerIdAndStageStageIdAndUnlockedTrue(
                        player.getPlayerId(), stage.getStageId());
    }

    /**
     * Applies one finished game to aggregate progress and personal bests.
     */
    @Transactional
    public void recordGameResult(Player player, Stage stage, int score, String result, int playTime) {
        initializePlayer(player);
        LocalDateTime now = LocalDateTime.now();
        PlayerStageProgress stageProgress = playerStageProgressRepository
                .findByPlayerPlayerIdAndStageStageId(player.getPlayerId(), stage.getStageId())
                .orElseGet(() -> PlayerStageProgress.builder()
                        .player(player)
                        .stage(stage)
                        .unlocked(true)
                        .completionCount(0)
                        .updatedAt(now)
                        .build());

        if (isBetterResult(stageProgress, score, playTime)) {
            stageProgress.setBestScore(score);
            stageProgress.setBestPlayTime(playTime);
        }

        if ("WIN".equalsIgnoreCase(result)) {
            boolean firstCompletion = stageProgress.getCompletionCount() == 0;
            stageProgress.setCompletionCount(stageProgress.getCompletionCount() + 1);
            player.setGold(player.getGold() + stage.getRewardGold());
            if (firstCompletion) {
                player.setCompletedStages(player.getCompletedStages() + 1);
            }
            unlockNextStage(player, stage, now);
        }

        stageProgress.setUnlocked(true);
        stageProgress.setUpdatedAt(now);
        player.setUpdatedAt(now);
        playerStageProgressRepository.save(stageProgress);
        playerRepository.save(player);
    }

    private boolean isBetterResult(PlayerStageProgress current, int score, int playTime) {
        return current.getBestScore() == null
                || score > current.getBestScore()
                || (score == current.getBestScore()
                && (current.getBestPlayTime() == null || playTime < current.getBestPlayTime()));
    }

    private void unlockNextStage(Player player, Stage completedStage, LocalDateTime now) {
        stageRepository.findFirstByStageIdGreaterThanOrderByStageIdAsc(completedStage.getStageId())
                .ifPresent(stage -> {
                    if (!playerStageProgressRepository
                            .existsByPlayerPlayerIdAndStageStageIdAndUnlockedTrue(
                                    player.getPlayerId(), stage.getStageId())) {
                        unlockStage(player, stage, now);
                    }
                });
    }

    private java.util.Optional<Stage> findFirstPlayableStage() {
        return stageRepository.findAllByOrderByStageIdAsc().stream()
                .filter(stage -> !stage.getPath().isEmpty() && !stage.getWaves().isEmpty())
                .findFirst();
    }

    private void unlockStage(Player player, Stage stage, LocalDateTime now) {
        PlayerStageProgress stageProgress = playerStageProgressRepository
                .findByPlayerPlayerIdAndStageStageId(player.getPlayerId(), stage.getStageId())
                .orElseGet(() -> PlayerStageProgress.builder()
                        .player(player)
                        .stage(stage)
                        .completionCount(0)
                        .build());
        stageProgress.setUnlocked(true);
        stageProgress.setUpdatedAt(now);
        playerStageProgressRepository.save(stageProgress);
    }

    private void unlockTower(Player player, Tower tower, LocalDateTime now) {
        playerTowerUnlockRepository.save(PlayerTowerUnlock.builder()
                .player(player)
                .tower(tower)
                .unlockedAt(now)
                .build());
    }

}
