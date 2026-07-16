package com.machinerift.machine_rift.service;

import com.machinerift.machine_rift.dto.TowerResponseDto;
import com.machinerift.machine_rift.entity.Tower;
import com.machinerift.machine_rift.exception.ResourceNotFoundException;
import com.machinerift.machine_rift.mapper.TowerMapper;
import com.machinerift.machine_rift.repository.TowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for tower-related read operations.
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

    /**
     * Retrieves a tower by id.
     *
     * @param id tower id
     * @return tower response DTO
     */
    @Transactional(readOnly = true)
    public TowerResponseDto getTowerById(Long id) {
        Tower tower = towerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tower not found with id: " + id));
        return towerMapper.toResponseDto(tower);
    }
}
