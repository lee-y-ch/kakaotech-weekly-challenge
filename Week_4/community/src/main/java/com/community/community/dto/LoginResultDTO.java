package com.community.community.dto;

import lombok.Getter;

@Getter
public class LoginResultDTO {

    private final LoginResponseDTO response;
    private final String accessToken;

    public LoginResultDTO(LoginResponseDTO response, String accessToken) {
        this.response = response;
        this.accessToken = accessToken;
    }
}
