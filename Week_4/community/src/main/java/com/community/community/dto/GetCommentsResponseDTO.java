package com.community.community.dto;

import java.util.List;

public class GetCommentsResponseDTO {
    private List<CommentListItemResponseDTO> comments;
    private PaginationResponseDTO pagination;

    public GetCommentsResponseDTO() {
    }

    public List<CommentListItemResponseDTO> getComments() {
        return comments;
    }

    public PaginationResponseDTO getPagination() {
        return pagination;
    }

    public void setComments(List<CommentListItemResponseDTO> comments) {
        this.comments = comments;
    }

    public void setPagination(PaginationResponseDTO pagination) {
        this.pagination = pagination;
    }
}
