package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PostCreateRequestDTO {

    @NotBlank(message = "invalid_create_post_request")
    @Size(max = 26, message = "invalid_create_post_request")
    private String title;

    @NotBlank(message = "invalid_create_post_request")
    private String content;

    @JsonProperty("image_url")
    private String imageUrl;
}
