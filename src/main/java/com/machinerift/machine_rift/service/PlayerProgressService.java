package com.machinerift.machine_rift.service;

import com.machinerift.machine_rift.dto.PlayerProgressResponseDto;
import com.machinerift.machine_rift.dto.PlayerStageProgressResponseDto;
import com.machinerift.machine_rift.dto.UnlockedTowerResponseDto;
import com.machinerift.machine_rift.entity.Player;
import com.machinerift.machine_rift.entity.PlayerProgress;
import com.machinerift.machine_rift.entity.PlayerStageProgress;
import com.machinerift.machine_rift.entity.PlayerTowerUnlock;
import com.machinerift.machine_rift.entity.Stage;
import com.machinerift.machine_rift.entity.Tower;
import com.machinerift.machine_rift.exception.ResourceNotFoundException;
import com.machinerift.machine_rift.repository.PlayerProgressRepository;
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

    private static final int EXPERIENCE_PER_LEVEL = 1000;

    private final PlayerRepository playerRepository;
    private final PlayerProgressRepository playerProgressRepository;
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
        if (!playerProgressRepository.existsById(player.getPlayerId())) {
            playerProgressRepository.save(PlayerProgress.builder()
                    .player(player)
                    .experience(0)
                    .gold(0)
                    .completedStages(0)
                    .updatedAt(now)
                    .build());
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
    }

    /**
     * Returns the full progression view, including locked stages.
     *
     * @param playerId player id
     * @return progression response
     */
    @Transactional
    public PlayerProgressResponseDto getProgress(Long playerId) {
        Player player = getPlayer(playerId);
        initializePlayer(player);

        PlayerProgress progress = playerProgressRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Progress not found for player: " + playerId));
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
                .level(player.getLevel())
                .experience(progress.getExperience())
                .gold(progress.getGold())
                .completedStages(progress.getCompletedStages())
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
        PlayerProgress progress = playerProgressRepository.findById(player.getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Progress not found for player: " + player.getPlayerId()));
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

        progress.setExperience(progress.getExperience() + score);
        if ("WIN".equalsIgnoreCase(result)) {
            boolean firstCompletion = stageProgress.getCompletionCount() == 0;
            stageProgress.setCompletionCount(stageProgress.getCompletionCount() + 1);
            progress.setGold(progress.getGold() + stage.getRewardGold());
            if (firstCompletion) {
                progress.setCompletedStages(progress.getCompletedStages() + 1);
            }
            unlockNextStage(player, stage, now);
            unlockNextTower(player, now);
        }

        player.setLevel(1 + progress.getExperience() / EXPERIENCE_PER_LEVEL);
        stageProgress.setUnlocked(true);
        stageProgress.setUpdatedAt(now);
        progress.setUpdatedAt(now);
        playerStageProgressRepository.save(stageProgress);
        playerProgressRepository.save(progress);
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

    private void unlockNextTower(Player player, LocalDateTime now) {
        towerRepository.findAllByOrderByCostAscTowerIdAsc().stream()
                .filter(tower -> !playerTowerUnlockRepository
                        .existsByPlayerPlayerIdAndTowerTowerId(
                                player.getPlayerId(), tower.getTowerId()))
                .findFirst()
                .ifPresent(tower -> unlockTower(player, tower, now));
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

    private Player getPlayer(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Player not found with id: " + playerId));
    }
}
