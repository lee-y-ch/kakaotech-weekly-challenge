package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserUpdateRequestDTO {

    private String nickname;

    @JsonProperty("profile_image")
    private String profileImage;

    public UserUpdateRequestDTO() {
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
