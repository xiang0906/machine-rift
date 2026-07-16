package com.machinerift.machine_rift.service;

import com.machinerift.machine_rift.exception.ResourceConflictException;
import com.machinerift.machine_rift.mapper.PlayerMapper;
import com.machinerift.machine_rift.mapper.StageMapper;
import com.machinerift.machine_rift.repository.GameRecordRepository;
import com.machinerift.machine_rift.repository.PlayerRepository;
import com.machinerift.machine_rift.repository.StageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeletionIntegrityServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private StageRepository stageRepository;

    @Mock
    private GameRecordRepository gameRecordRepository;

    private PlayerService playerService;
    private StageService stageService;

    @BeforeEach
    void setUp() {
        playerService = new PlayerService(playerRepository, gameRecordRepository, new PlayerMapper());
        stageService = new StageService(stageRepository, gameRecordRepository, new StageMapper());
    }

    @Test
    void shouldRejectDeletingPlayerReferencedByGameRecords() {
        when(playerRepository.existsById(1L)).thenReturn(true);
        when(gameRecordRepository.existsByPlayerPlayerId(1L)).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> playerService.deletePlayer(1L));

        verify(playerRepository, never()).deleteById(1L);
    }

    @Test
    void shouldRejectDeletingStageReferencedByGameRecords() {
        when(stageRepository.existsById(1L)).thenReturn(true);
        when(gameRecordRepository.existsByStageStageId(1L)).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> stageService.deleteStage(1L));

        verify(stageRepository, never()).deleteById(1L);
    }

    @Test
    void shouldDeletePlayerWithoutGameRecords() {
        when(playerRepository.existsById(1L)).thenReturn(true);
        when(gameRecordRepository.existsByPlayerPlayerId(1L)).thenReturn(false);

        playerService.deletePlayer(1L);

        verify(playerRepository).deleteById(1L);
    }
}
