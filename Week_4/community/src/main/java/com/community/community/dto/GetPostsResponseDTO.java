package com.community.community.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class GetPostsResponseDTO {

    private final List<PostListItemResponseDTO> posts;
    private final PaginationResponseDTO pagination;

    public GetPostsResponseDTO(
            List<PostListItemResponseDTO> posts,
            PaginationResponseDTO pagination
    ) {
        this.posts = posts;
        this.pagination = pagination;
    }
}
