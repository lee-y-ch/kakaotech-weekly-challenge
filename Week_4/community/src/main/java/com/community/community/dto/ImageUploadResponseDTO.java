package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageUploadResponseDTO {

    @JsonProperty("image_url")
    private String imageUrl;
}
