package com.community.community.dto;

import java.util.List;

public class GetPostsResponseDTO {

    private List<PostListItemResponseDTO> posts;
    private PaginationResponseDTO pagination;

    public GetPostsResponseDTO() {
    }

    public void setPosts(List<PostListItemResponseDTO> posts) {
        this.posts = posts;
    }

    public void setPagination(PaginationResponseDTO pagination) {
        this.pagination = pagination;
    }

    public List<PostListItemResponseDTO> getPosts() {
        return posts;
    }

    public PaginationResponseDTO getPagination() {
        return pagination;
    }
}
