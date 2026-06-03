package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostUpdateResponseDTO {
    @JsonProperty("post_id")
    private int postId;

    public PostUpdateResponseDTO() {
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }
}
