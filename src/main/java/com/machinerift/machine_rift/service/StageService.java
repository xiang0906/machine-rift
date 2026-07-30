package com.machinerift.machine_rift.service;

import com.machinerift.machine_rift.dto.StageResponseDto;
import com.machinerift.machine_rift.mapper.StageMapper;
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

}
