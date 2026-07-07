package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageStatusResponseDTO {

    // Lambda 처리 확인
    private boolean ready;

    @JsonProperty("processed_key")
    private String processedKey;

    @JsonProperty("image_url")
    private String imageUrl;
}
