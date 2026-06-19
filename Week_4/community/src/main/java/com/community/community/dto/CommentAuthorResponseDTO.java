package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CommentAuthorResponseDTO {

    @JsonProperty("user_id")
    private final Integer userId;

    private final String nickname;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    public CommentAuthorResponseDTO(Integer userId, String nickname, String profileImageUrl) {
        this.userId = userId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }
}
