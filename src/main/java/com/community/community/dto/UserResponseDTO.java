package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

// api 중에 User 정보를 해당 형태로 응답하는 경우가 많기 때문에 공통으로 사용하기 위한 DTO
@Getter
public class UserResponseDTO {

    @JsonProperty("user_id")
    private final Integer userId;

    private final String email;
    private final String nickname;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    public UserResponseDTO(Integer userId, String email, String nickname, String profileImageUrl) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }
}
