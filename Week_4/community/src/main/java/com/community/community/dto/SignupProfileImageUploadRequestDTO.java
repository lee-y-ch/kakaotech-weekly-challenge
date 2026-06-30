package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupProfileImageUploadRequestDTO {

    @JsonProperty("content_type")
    @NotBlank(message = "invalid_image_upload_request")
    private String contentType;

    @JsonProperty("file_size")
    @NotNull(message = "invalid_image_upload_request")
    @Positive(message = "invalid_image_upload_request")
    private Long fileSize;
}
