package com.community.community.dto;

public class GetPostResponseDTO {

    private PostResponseDTO post;

    public GetPostResponseDTO() {
    }

    public GetPostResponseDTO(PostResponseDTO post) {
        this.post = post;
    }

    public PostResponseDTO getPost() {
        return post;
    }

    public void setPost(PostResponseDTO post) {
        this.post = post;
    }
}
