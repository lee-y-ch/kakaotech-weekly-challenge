package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CommentCreateResponseDTO {

    @JsonProperty("comment_id")
    private final Integer commentId;

    public CommentCreateResponseDTO(Integer commentId) {
        this.commentId = commentId;
    }
}
