package com.community.community.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// /auth/login 요청에 대한 DTO
@Getter @Setter
public class LoginRequestDTO {

    @NotBlank(message = "invalid_login_request")
    @Email(message = "invalid_login_request")
    private String email;

    @NotBlank(message = "invalid_login_request")
    private String password;
}
