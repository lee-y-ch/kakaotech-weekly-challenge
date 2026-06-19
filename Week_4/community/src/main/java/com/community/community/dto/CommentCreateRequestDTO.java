package com.community.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CommentCreateRequestDTO {

    @NotBlank(message = "invalid_create_comment_request")
    private String content;
}
