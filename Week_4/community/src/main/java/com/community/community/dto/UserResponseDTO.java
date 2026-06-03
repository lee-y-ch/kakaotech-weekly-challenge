package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// api 중에 User 정보를 해당 형태로 응답하는 경우가 많기 때문에 공통으로 사용하기 위한 DTO
public class UserResponseDTO {

    @JsonProperty("user_id")
    private int userId;

    private String email;
    private String nickname;

    @JsonProperty("profile_image")
    private String profileImage;

    public UserResponseDTO() {

    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
