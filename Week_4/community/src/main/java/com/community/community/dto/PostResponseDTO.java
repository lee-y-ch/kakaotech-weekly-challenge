package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PostResponseDTO {

    @JsonProperty("post_id")
    private final Integer postId;

    private final String title;
    private final String content;

    @JsonProperty("image_url")
    private final String imageUrl;

    private final PostAuthorResponseDTO author;

    @JsonProperty("created_at")
    private final String createdAt;

    @JsonProperty("like_count")
    private final int likeCount;

    @JsonProperty("comment_count")
    private final int commentCount;

    @JsonProperty("view_count")
    private final int viewCount;

    @JsonProperty("is_liked")
    private final boolean liked;

    @JsonProperty("is_author")
    private final boolean writtenByCurrentUser;

    public PostResponseDTO(
            Integer postId,
            String title,
            String content,
            String imageUrl,
            PostAuthorResponseDTO author,
            String createdAt,
            int likeCount,
            int commentCount,
            int viewCount,
            boolean liked,
            boolean writtenByCurrentUser
    ) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.author = author;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.liked = liked;
        this.writtenByCurrentUser = writtenByCurrentUser;
    }
}
