package com.machinerift.machine_rift.service;

import com.machinerift.machine_rift.dto.TowerRequestDto;
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

    /**
     * Creates a new tower.
     *
     * @param requestDto tower creation payload
     * @return created tower response DTO
     */
    @Transactional
    public TowerResponseDto createTower(TowerRequestDto requestDto) {
        Tower tower = towerMapper.toEntity(requestDto);
        return towerMapper.toResponseDto(towerRepository.save(tower));
    }

    /**
     * Updates an existing tower.
     *
     * @param id tower id
     * @param requestDto update payload
     * @return updated tower response DTO
     */
    @Transactional
    public TowerResponseDto updateTower(Long id, TowerRequestDto requestDto) {
        Tower tower = towerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tower not found with id: " + id));
        towerMapper.updateEntity(tower, requestDto);
        return towerMapper.toResponseDto(towerRepository.save(tower));
    }

    /**
     * Deletes a tower by id.
     *
     * @param id tower id
     */
    @Transactional
    public void deleteTower(Long id) {
        if (!towerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tower not found with id: " + id);
        }
        towerRepository.deleteById(id);
    }
}
