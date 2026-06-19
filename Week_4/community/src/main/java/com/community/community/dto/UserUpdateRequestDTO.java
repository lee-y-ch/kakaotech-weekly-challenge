package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserUpdateRequestDTO {

    @NotBlank(message = "invalid_update_user_request")
    @Size(max = 10, message = "invalid_update_user_request")
    private String nickname;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;
}
