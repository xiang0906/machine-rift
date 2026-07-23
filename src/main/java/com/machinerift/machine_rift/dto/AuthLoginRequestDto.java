package com.machinerift.machine_rift.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthLoginRequestDto {

    @NotBlank(message = "請輸入帳號")
    private String username;

    @NotBlank(message = "請輸入密碼")
    private String password;
}
