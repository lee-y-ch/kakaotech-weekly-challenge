package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CommentUpdateResponseDTO {

    @JsonProperty("comment_id")
    private final Integer commentId;

    private final String content;

    public CommentUpdateResponseDTO(Integer commentId, String content) {
        this.commentId = commentId;
        this.content = content;
    }
}