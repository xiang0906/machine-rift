package com.machinerift.machine_rift.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthResponseDto {

    private String accessToken;
    private LocalDateTime expiresAt;
    private PlayerResponseDto player;
}
