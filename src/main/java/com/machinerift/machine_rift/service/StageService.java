package com.machinerift.machine_rift.service;

import com.machinerift.machine_rift.dto.StageRequestDto;
import com.machinerift.machine_rift.dto.StageResponseDto;
import com.machinerift.machine_rift.entity.Stage;
import com.machinerift.machine_rift.exception.ResourceConflictException;
import com.machinerift.machine_rift.exception.ResourceNotFoundException;
import com.machinerift.machine_rift.mapper.StageMapper;
import com.machinerift.machine_rift.repository.GameRecordRepository;
import com.machinerift.machine_rift.repository.StageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for stage-related operations.
 */
@Service
@RequiredArgsConstructor
public class StageService {

    private final StageRepository stageRepository;
    private final GameRecordRepository gameRecordRepository;
    private final StageMapper stageMapper;

    /**
     * Retrieves all stages.
     *
     * @return list of stage response DTOs
     */
    @Transactional(readOnly = true)
    public List<StageResponseDto> getAllStages() {
        return stageRepository.findAll().stream()
                .map(stageMapper::toResponseDto)
                .toList();
    }

    /**
     * Retrieves a stage by id.
     *
     * @param id stage id
     * @return stage response DTO
     */
    @Transactional(readOnly = true)
    public StageResponseDto getStageById(Long id) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found with id: " + id));
        return stageMapper.toResponseDto(stage);
    }

    /**
     * Creates a new stage.
     *
     * @param requestDto stage creation payload
     * @return created stage response DTO
     */
    @Transactional
    public StageResponseDto createStage(StageRequestDto requestDto) {
        Stage stage = stageMapper.toEntity(requestDto);
        return stageMapper.toResponseDto(stageRepository.save(stage));
    }

    /**
     * Updates an existing stage.
     *
     * @param id stage id
     * @param requestDto update payload
     * @return updated stage response DTO
     */
    @Transactional
    public StageResponseDto updateStage(Long id, StageRequestDto requestDto) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found with id: " + id));
        stageMapper.updateEntity(stage, requestDto);
        return stageMapper.toResponseDto(stageRepository.save(stage));
    }

    /**
     * Deletes a stage by id.
     *
     * @param id stage id
     */
    @Transactional
    public void deleteStage(Long id) {
        if (!stageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Stage not found with id: " + id);
        }
        if (gameRecordRepository.existsByStageStageId(id)) {
            throw new ResourceConflictException("Cannot delete stage with existing game records.");
        }
        stageRepository.deleteById(id);
    }
}
