package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PostListItemResponseDTO {

    @JsonProperty("post_id")
    private final Integer postId;

    private final String title;
    private final PostAuthorResponseDTO author;

    @JsonProperty("created_at")
    private final String createdAt;

    @JsonProperty("like_count")
    private final int likeCount;

    @JsonProperty("comment_count")
    private final int commentCount;

    @JsonProperty("view_count")
    private final int viewCount;

    public PostListItemResponseDTO(
            Integer postId,
            String title,
            PostAuthorResponseDTO author,
            String createdAt,
            int likeCount,
            int commentCount,
            int viewCount
    ) {
        this.postId = postId;
        this.title = title;
        this.author = author;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
    }
}
