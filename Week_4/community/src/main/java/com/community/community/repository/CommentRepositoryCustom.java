package com.community.community.repository;

import com.community.community.dto.CommentListItemResponseDTO;

import java.util.List;

public interface CommentRepositoryCustom {

    List<CommentListItemResponseDTO> findCommentListByCursor(
            int postId,
            int cursor,
            int limit,
            // currentUserId는 현재 로그인한 user_id == 댓글 작성자 user_id를 비교하여
            // "is_author": true를 검증할 때 사용
            int currentUserId
    );
}
