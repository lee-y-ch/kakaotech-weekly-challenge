package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PasswordUpdateRequestDTO {

    private String password;

    @JsonProperty("password_confirm")
    private String passwordConfirm;

    public PasswordUpdateRequestDTO() {
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }
}
