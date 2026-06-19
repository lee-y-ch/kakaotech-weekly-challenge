package com.community.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CommentUpdateRequestDTO {

    @NotBlank(message = "invalid_update_comment_request")
    private String content;
}
