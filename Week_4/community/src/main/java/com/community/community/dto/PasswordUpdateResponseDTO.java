package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PasswordUpdateResponseDTO {

    @JsonProperty("user_id")
    private int userId;

    public PasswordUpdateResponseDTO() {
    }

    public PasswordUpdateResponseDTO(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
