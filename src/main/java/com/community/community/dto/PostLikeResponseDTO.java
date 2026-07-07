package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PostLikeResponseDTO {

    @JsonProperty("post_id")
    private final Integer postId;

    @JsonProperty("is_liked")
    private final boolean liked;

    @JsonProperty("like_count")
    private final int likeCount;

    public PostLikeResponseDTO(Integer postId, boolean liked, int likeCount) {
        this.postId = postId;
        this.liked = liked;
        this.likeCount = likeCount;
    }
}
