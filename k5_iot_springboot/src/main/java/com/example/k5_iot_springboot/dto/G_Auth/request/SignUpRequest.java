package com.example.k5_iot_springboot.dto.G_Auth.request;


import com.example.k5_iot_springboot.common.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 DTO
 */

public record SignUpRequest(
        @NotBlank @Size(min = 4, max = 50) String loginId,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(max = 50) String nickname,
        Gender gender
) {

}
