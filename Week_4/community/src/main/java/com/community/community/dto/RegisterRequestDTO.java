package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// Jackson JSON 변환 라이브러리가 api 요청 body를 해당 DTO로 객체로 변환
public class RegisterRequestDTO {

    private String email;
    private String password;
    private String nickname;

    // 명세에 profile_image인 값을 profileImage 변수와 매핑시키기 위해 JsonProperty 사용
    @JsonProperty("profile_image")
    private String profileImage;

    public RegisterRequestDTO() {
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getProfileImage() {
        return profileImage;
    }
}
