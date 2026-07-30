package com.machinerift.machine_rift.service;

import com.machinerift.machine_rift.dto.TowerResponseDto;
import com.machinerift.machine_rift.mapper.TowerMapper;
import com.machinerift.machine_rift.repository.TowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for tower-related operations.
 */
@Service
@RequiredArgsConstructor
public class TowerService {

    private final TowerRepository towerRepository;
    private final TowerMapper towerMapper;

    /**
     * Retrieves all towers.
     *
     * @return list of tower response DTOs
     */
    @Transactional(readOnly = true)
    public List<TowerResponseDto> getAllTowers() {
        return towerRepository.findAll().stream()
                .map(towerMapper::toResponseDto)
                .toList();
    }

}
