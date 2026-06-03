package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CommentUpdateResponseDTO {
    @JsonProperty("comment_id")
    private int commentId;

    private String content;

    public CommentUpdateResponseDTO() {
    }

    public int getCommentId() {
        return commentId;
    }

    public String getContent() {
        return content;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
