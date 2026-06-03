package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostLikeResponseDTO {

    @JsonProperty("post_id")
    private int postId;

    @JsonProperty("is_liked")
    private boolean liked;

    @JsonProperty("like_count")
    private int likeCount;

    public PostLikeResponseDTO() {
    }

    public int getPostId() {
        return postId;
    }

    public boolean isLiked() {
        return liked;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}
