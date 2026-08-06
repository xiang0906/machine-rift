package com.machinerift.machine_rift.service;

import com.machinerift.machine_rift.dto.TowerResponseDto;
import com.machinerift.machine_rift.dto.TowerUnlockResponseDto;
import com.machinerift.machine_rift.entity.Player;
import com.machinerift.machine_rift.entity.PlayerTowerUnlock;
import com.machinerift.machine_rift.entity.Tower;
import com.machinerift.machine_rift.exception.ResourceConflictException;
import com.machinerift.machine_rift.exception.ResourceNotFoundException;
import com.machinerift.machine_rift.mapper.TowerMapper;
import com.machinerift.machine_rift.repository.PlayerRepository;
import com.machinerift.machine_rift.repository.PlayerTowerUnlockRepository;
import com.machinerift.machine_rift.repository.TowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for tower-related operations.
 */
@Service
@RequiredArgsConstructor
public class TowerService {

    private final TowerRepository towerRepository;
    private final PlayerRepository playerRepository;
    private final PlayerTowerUnlockRepository playerTowerUnlockRepository;
    private final TowerMapper towerMapper;

    /**
     * Retrieves all towers.
     *
     * @return list of tower response DTOs
     */
    @Transactional(readOnly = true)
    public List<TowerResponseDto> getAllTowers() {
        return towerRepository.findAllByOrderByCostAscTowerIdAsc().stream()
                .map(towerMapper::toResponseDto)
                .toList();
    }

    /**
     * Permanently unlocks one tower for the authenticated player.
     */
    @Transactional
    public TowerUnlockResponseDto unlockTower(Long towerId, Player authenticatedPlayer) {
        Tower tower = towerRepository.findById(towerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "找不到指定的防禦塔，ID：" + towerId));
        Player player = playerRepository.findByIdForUpdate(authenticatedPlayer.getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "找不到目前登入的玩家"));

        if (playerTowerUnlockRepository
                .existsByPlayerPlayerIdAndTowerTowerId(player.getPlayerId(), towerId)) {
            throw new ResourceConflictException("此防禦塔已經解鎖");
        }
        if (player.getGold() < tower.getUnlockCost()) {
            throw new ResourceConflictException(
                    "永久戰備金不足，需要 " + tower.getUnlockCost() + " G");
        }

        LocalDateTime now = LocalDateTime.now();
        player.setGold(player.getGold() - tower.getUnlockCost());
        player.setUpdatedAt(now);
        playerRepository.save(player);
        playerTowerUnlockRepository.save(PlayerTowerUnlock.builder()
                .player(player)
                .tower(tower)
                .unlockedAt(now)
                .build());

        return TowerUnlockResponseDto.builder()
                .towerId(tower.getTowerId())
                .towerName(tower.getTowerName())
                .unlockCost(tower.getUnlockCost())
                .remainingGold(player.getGold())
                .unlockedAt(now)
                .build();
    }

}
