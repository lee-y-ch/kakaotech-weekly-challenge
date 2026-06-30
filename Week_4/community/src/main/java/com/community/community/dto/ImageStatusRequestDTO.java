package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ImageStatusRequestDTO {

    @JsonProperty("original_key")
    @NotBlank(message = "invalid_image_upload_request")
    private String originalKey;

    @JsonProperty("processed_key")
    @NotBlank(message = "invalid_image_upload_request")
    private String processedKey;
}
