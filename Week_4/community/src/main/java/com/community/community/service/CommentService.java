package com.community.community.service;

import com.community.community.dto.*;
import com.community.community.repository.CommentRepository;
import com.community.community.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public CommentService(PostRepository postRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    // 댓글 저장과 posts.comment_count 증가 중 하나만 반영되는 것을 막기 위해 트랜잭션으로 묶는다.
    @Transactional
    public CommentCreateResponseDTO createComment(
            int postId,
            int currentUserId,
            CommentCreateRequestDTO request
    ) {
        if (postRepository.findById(postId) == null) {
            throw new IllegalStateException("post_not_found");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("invalid_create_comment_request");
        }

        int commentId = commentRepository.save(
                postId,
                currentUserId,
                request.getContent()
        );

        postRepository.increaseCommentCount(postId);

        CommentCreateResponseDTO commentCreateResponseDTO = new CommentCreateResponseDTO();
        commentCreateResponseDTO.setCommentId(commentId);

        return commentCreateResponseDTO;
    }

    public GetCommentsResponseDTO getComments(
            int postId,
            String cursorValue,
            String sizeValue,
            int currentUserId
    ) {
        int cursor;
        int size;

        try {
            cursor = Integer.parseInt(cursorValue);
            size = Integer.parseInt(sizeValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid_comments_request");
        }

        if (cursor < 0 || size <= 0) {
            throw new IllegalArgumentException("invalid_comments_request");
        }

        if (postRepository.findById(postId) == null) {
            throw new IllegalStateException("post_not_found");
        }

        // 화면에 보여줄 개수보다 하나 더 조회하여 다음 페이지 존재 여부를 판단한다.
        List<CommentListItemResponseDTO> comments =
                commentRepository.findCommentsByCursor(
                        postId,
                        cursor,
                        size + 1,
                        currentUserId
                );

        boolean hasNext = comments.size() > size;

        if (hasNext) {
            comments = comments.subList(0, size);
        }

        Integer nextCursor = null;

        // 다음 조회는 comment_id < cursor 조건을 사용하므로 마지막으로 응답한 ID 자체를 전달한다.
        if (hasNext && !comments.isEmpty()) {
            nextCursor = comments.get(comments.size() - 1).getCommentId();
        }

        PaginationResponseDTO pagination = new PaginationResponseDTO();
        pagination.setNextCursor(nextCursor);
        pagination.setHasNext(hasNext);

        GetCommentsResponseDTO response = new GetCommentsResponseDTO();
        response.setComments(comments);
        response.setPagination(pagination);

        return response;
    }

    public CommentUpdateResponseDTO updateComment(
            int commentId,
            int currentUserId,
            CommentUpdateRequestDTO request
    ) {
        // 댓글 내용은 필수이므로 공백만 입력한 경우도 저장하지 않는다.
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("invalid_update_comment_request");
        }

        Integer authorId = commentRepository.findAuthorIdByCommentId(commentId);

        if (authorId == null) {
            throw new IllegalStateException("comment_not_found");
        }

        if (authorId != currentUserId) {
            throw new SecurityException("forbidden");
        }

        commentRepository.updateContent(commentId, request.getContent());

        CommentUpdateResponseDTO response = new CommentUpdateResponseDTO();
        response.setCommentId(commentId);
        response.setContent(request.getContent());

        return response;
    }

    // 댓글 삭제와 comment_count 감소 중 하나만 반영되는 것을 막기 위해 트랜잭션으로 묶는다.
    @Transactional
    public void deleteComment(int commentId, int currentUserId) {
        Integer authorId = commentRepository.findAuthorIdByCommentId(commentId);

        if (authorId == null) {
            throw new IllegalStateException("comment_not_found");
        }

        if (authorId != currentUserId) {
            throw new SecurityException("forbidden");
        }

        Integer postId = commentRepository.findPostIdByCommentId(commentId);

        if (postId == null) {
            throw new IllegalStateException("comment_not_found");
        }

        int deletedCount = commentRepository.deleteById(commentId);

        if (deletedCount == 0) {
            throw new IllegalStateException("comment_not_found");
        }

        postRepository.decreaseCommentCount(postId);
    }


}
