package com.community.community.dto;

import lombok.Getter;

@Getter
public class LoginResponseDTO {

    private final UserResponseDTO user;

    public LoginResponseDTO(UserResponseDTO user) {
        this.user = user;
    }
}
