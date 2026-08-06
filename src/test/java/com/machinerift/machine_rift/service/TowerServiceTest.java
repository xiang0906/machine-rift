package com.machinerift.machine_rift.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TowerServiceTest {

    @Mock
    private TowerRepository towerRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerTowerUnlockRepository playerTowerUnlockRepository;

    @Mock
    private TowerMapper towerMapper;

    private TowerService towerService;

    @BeforeEach
    void setUp() {
        towerService = new TowerService(
                towerRepository,
                playerRepository,
                playerTowerUnlockRepository,
                towerMapper);
    }

    @Test
    void shouldDeductGoldAndPersistSelectedTowerUnlock() {
        Player authenticatedPlayer = Player.builder().playerId(1L).build();
        Player lockedPlayer = player(1L, 500);
        Tower tower = tower(2L, "離子機槍塔", 250);
        when(towerRepository.findById(2L)).thenReturn(Optional.of(tower));
        when(playerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lockedPlayer));
        when(playerTowerUnlockRepository
                .existsByPlayerPlayerIdAndTowerTowerId(1L, 2L)).thenReturn(false);

        TowerUnlockResponseDto response = towerService.unlockTower(2L, authenticatedPlayer);

        ArgumentCaptor<PlayerTowerUnlock> unlockCaptor =
                ArgumentCaptor.forClass(PlayerTowerUnlock.class);
        verify(playerTowerUnlockRepository).save(unlockCaptor.capture());
        verify(playerRepository).save(lockedPlayer);
        assertEquals(250, lockedPlayer.getGold());
        assertEquals(250, response.getUnlockCost());
        assertEquals(250, response.getRemainingGold());
        assertEquals(2L, unlockCaptor.getValue().getTower().getTowerId());
        assertEquals(1L, unlockCaptor.getValue().getPlayer().getPlayerId());
    }

    @Test
    void shouldRejectPurchaseWhenPermanentGoldIsInsufficient() {
        Player authenticatedPlayer = Player.builder().playerId(1L).build();
        Player lockedPlayer = player(1L, 100);
        Tower tower = tower(2L, "離子機槍塔", 250);
        when(towerRepository.findById(2L)).thenReturn(Optional.of(tower));
        when(playerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lockedPlayer));
        when(playerTowerUnlockRepository
                .existsByPlayerPlayerIdAndTowerTowerId(1L, 2L)).thenReturn(false);

        ResourceConflictException exception = assertThrows(
                ResourceConflictException.class,
                () -> towerService.unlockTower(2L, authenticatedPlayer));

        assertEquals("永久戰備金不足，需要 250 G", exception.getMessage());
        assertEquals(100, lockedPlayer.getGold());
        verify(playerRepository, never()).save(lockedPlayer);
        verify(playerTowerUnlockRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectDuplicateTowerPurchaseWithoutDeductingGold() {
        Player authenticatedPlayer = Player.builder().playerId(1L).build();
        Player lockedPlayer = player(1L, 500);
        Tower tower = tower(2L, "離子機槍塔", 250);
        when(towerRepository.findById(2L)).thenReturn(Optional.of(tower));
        when(playerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lockedPlayer));
        when(playerTowerUnlockRepository
                .existsByPlayerPlayerIdAndTowerTowerId(1L, 2L)).thenReturn(true);

        ResourceConflictException exception = assertThrows(
                ResourceConflictException.class,
                () -> towerService.unlockTower(2L, authenticatedPlayer));

        assertEquals("此防禦塔已經解鎖", exception.getMessage());
        assertEquals(500, lockedPlayer.getGold());
        verify(playerRepository, never()).save(lockedPlayer);
        verify(playerTowerUnlockRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldReturnChineseMessageWhenTowerDoesNotExist() {
        Player authenticatedPlayer = Player.builder().playerId(1L).build();
        when(towerRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> towerService.unlockTower(99L, authenticatedPlayer));

        assertEquals("找不到指定的防禦塔，ID：99", exception.getMessage());
        verify(playerRepository, never()).findByIdForUpdate(1L);
    }

    private Player player(Long id, int gold) {
        return Player.builder().playerId(id).gold(gold).build();
    }

    private Tower tower(Long id, String name, int unlockCost) {
        return Tower.builder()
                .towerId(id)
                .towerName(name)
                .unlockCost(unlockCost)
                .build();
    }
}
