package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponseDTO {

    @JsonProperty("session_id")
    private String sessionId;

    public LoginResponseDTO() {

    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
