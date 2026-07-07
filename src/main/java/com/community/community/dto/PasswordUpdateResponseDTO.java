package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PasswordUpdateResponseDTO {

    @JsonProperty("user_id")
    private final Integer userId;

    public PasswordUpdateResponseDTO(Integer userId) {
        this.userId = userId;
    }
}
