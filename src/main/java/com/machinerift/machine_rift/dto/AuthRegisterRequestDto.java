package com.machinerift.machine_rift.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRegisterRequestDto {

    @NotBlank(message = "請輸入帳號")
    @Pattern(regexp = "^[^\\s\\p{Cntrl}]+$", message = "帳號可以使用特殊符號，但不能包含空白")
    @Size(min = 3, max = 50, message = "帳號長度必須為 3～50 個字元")
    private String username;

    @NotBlank(message = "請輸入密碼")
    @Size(min = 8, max = 72, message = "密碼長度必須為 8～72 個字元")
    private String password;

    @NotBlank(message = "請輸入玩家顯示名稱")
    @Size(max = 100, message = "玩家顯示名稱最多 100 個字元")
    private String playerName;
}
