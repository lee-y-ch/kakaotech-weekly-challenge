package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignedImageUploadResponseDTO {

    @JsonProperty("upload_url")
    private String uploadUrl;

    @JsonProperty("original_key")
    private String originalKey;

    @JsonProperty("processed_key")
    private String processedKey;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("expires_in_seconds")
    private long expiresInSeconds;
}