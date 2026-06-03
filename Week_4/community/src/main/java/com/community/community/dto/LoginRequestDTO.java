package com.community.community.dto;

// /auth/login 요청에 대한 DTO
public class LoginRequestDTO {

    private String email;
    private String password;

    public LoginRequestDTO() {

    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
