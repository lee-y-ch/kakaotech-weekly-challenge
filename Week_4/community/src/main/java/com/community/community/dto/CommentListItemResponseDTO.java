package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CommentListItemResponseDTO {

    @JsonProperty("comment_id")
    private final Integer commentId;

    private final String content;

    @JsonProperty("created_at")
    private final String createdAt;

    @JsonProperty("is_author")
    private final boolean isAuthor;

    private final CommentAuthorResponseDTO author;

    public CommentListItemResponseDTO(
            Integer commentId,
            String content,
            String createdAt,
            boolean isAuthor,
            CommentAuthorResponseDTO author
    ) {
        this.commentId = commentId;
        this.content = content;
        this.createdAt = createdAt;
        this.isAuthor = isAuthor;
        this.author = author;
    }
}
