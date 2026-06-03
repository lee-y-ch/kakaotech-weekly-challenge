package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CommentCreateResponseDTO {

    @JsonProperty("comment_id")
    private int commentId;

    public CommentCreateResponseDTO() {
    }

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }
}
