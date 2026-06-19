package com.community.community.dto;

import lombok.Getter;

@Getter
public class CreatePostResponseDTO {

    private final CreatedPostResponseDTO post;

    public CreatePostResponseDTO(CreatedPostResponseDTO post) {
        this.post = post;
    }
}

