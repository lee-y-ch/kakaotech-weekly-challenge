package com.community.community.dto;

public class CreatePostResponseDTO {

    private PostResponseDTO post;

    public CreatePostResponseDTO() {
    }

    public CreatePostResponseDTO(PostResponseDTO post) {
        this.post = post;
    }

    public PostResponseDTO getPost() {
        return post;
    }

    public void setPost(PostResponseDTO post) {
        this.post = post;
    }
}
