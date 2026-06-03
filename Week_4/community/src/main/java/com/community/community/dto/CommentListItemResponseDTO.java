package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CommentListItemResponseDTO {
    @JsonProperty("comment_id")
    private int commentId;

    private String content;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("is_author")
    private boolean isAuthor;

    private CommentAuthorResponseDTO author;

    public CommentListItemResponseDTO() {
    }

    public int getCommentId() {
        return commentId;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isAuthor() {
        return isAuthor;
    }

    public CommentAuthorResponseDTO getAuthor() {
        return author;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setIsAuthor(boolean isauthor) {
        this.isAuthor = isauthor;
    }

    public void setAuthor(CommentAuthorResponseDTO author) {
        this.author = author;
    }
}
