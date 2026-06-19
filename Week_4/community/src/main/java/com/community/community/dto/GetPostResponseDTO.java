package com.community.community.dto;

import lombok.Getter;

@Getter
public class GetPostResponseDTO {

    private final PostResponseDTO post;

    public GetPostResponseDTO(PostResponseDTO post) {
        this.post = post;
    }
}
