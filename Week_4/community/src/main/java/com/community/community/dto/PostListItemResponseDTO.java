package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostListItemResponseDTO {

    @JsonProperty("post_id")
    private int postId;

    private String title;
    private PostAuthorResponseDTO author;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("like_count")
    private int likeCount;

    @JsonProperty("comment_count")
    private int commentCount;

    @JsonProperty("view_count")
    private int viewCount;

    public PostListItemResponseDTO() {
    }

    public int getPostId() {
        return postId;
    }

    public String getTitle() {
        return title;
    }

    public PostAuthorResponseDTO getAuthor() {
        return author;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(PostAuthorResponseDTO author) {
        this.author = author;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
}
