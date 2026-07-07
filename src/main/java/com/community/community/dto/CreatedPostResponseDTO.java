package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CreatedPostResponseDTO {

    @JsonProperty("post_id")
    private final Integer postId;

    public CreatedPostResponseDTO(Integer postId) {
        this.postId = postId;
    }
}
