package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CommentAuthorResponseDTO {

    @JsonProperty("user_id")
    private int userId;

    private String nickname;

    @JsonProperty("profile_image")
    private String profileImage;

    public CommentAuthorResponseDTO() {
    }

    public int getUserId() {
        return userId;
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

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
