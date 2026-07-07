package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequestDTO {

    @NotBlank(message = "invalid_register_request")
    @Email(message = "invalid_register_request")
    private String email;

    @NotBlank(message = "invalid_register_request")
    private String password;

    @NotBlank(message = "invalid_register_request")
    @Size(max = 10, message = "invalid_register_request")
    private String nickname;

    @JsonProperty("profile_image_url")
    @NotBlank(message = "invalid_register_request")
    private String profileImageUrl;
}