package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PostUpdateResponseDTO {

    @JsonProperty("post_id")
    private final Integer postId;

    public PostUpdateResponseDTO(Integer postId) {
        this.postId = postId;
    }
}
