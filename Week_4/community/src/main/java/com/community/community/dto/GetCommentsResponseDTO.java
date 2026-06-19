package com.community.community.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class GetCommentsResponseDTO {

    private final List<CommentListItemResponseDTO> comments;
    private final PaginationResponseDTO pagination;

    public GetCommentsResponseDTO(
            List<CommentListItemResponseDTO> comments,
            PaginationResponseDTO pagination
    ) {
        this.comments = comments;
        this.pagination = pagination;
    }
}
