package com.community.community.service;

import com.community.community.dto.*;
import com.community.community.entity.Comment;
import com.community.community.entity.Post;
import com.community.community.entity.User;
import com.community.community.exception.BusinessException;
import com.community.community.exception.ErrorCode;
import com.community.community.repository.CommentRepository;
import com.community.community.repository.PostRepository;
import com.community.community.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public CommentService(
            PostRepository postRepository,
            CommentRepository commentRepository,
            UserRepository userRepository
    ) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    // 댓글 저장과 posts.comment_count 증가 중 하나만 반영되는 것을 막기 위해 트랜잭션으로 묶는다.
    @Transactional
    public CommentCreateResponseDTO createComment(
            int postId,
            int currentUserId,
            CommentCreateRequestDTO request
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        User writer = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        Comment comment = new Comment(post, writer, request.getContent());
        Comment savedComment = commentRepository.save(comment);

        // comment_count update query가 영속성 컨텍스트를 정리할 수 있으므로 응답에 필요한 ID를 먼저 확보한다.
        Integer commentId = savedComment.getCommentId();

        postRepository.increaseCommentCount(postId);

        return new CommentCreateResponseDTO(commentId);
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
            throw new BusinessException(ErrorCode.INVALID_COMMENTS_REQUEST);
        }

        if (cursor < 0 || size <= 0) {
            throw new BusinessException(ErrorCode.INVALID_COMMENTS_REQUEST);
        }

        /*
         * 댓글 목록은 특정 게시글에 속한 댓글을 조회하는 기능이므로,
         * 먼저 게시글이 존재하는지 확인한다.
         * 게시글이 없다면 댓글 목록도 조회할 수 없으므로 post_not_found를 반환한다.
         */
        if (!postRepository.existsById(postId)) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        /*
         * 다음 페이지 존재 여부를 확인하기 위해 요청 size보다 1개 더 조회한다.
         */
        List<CommentListItemResponseDTO> comments = commentRepository.findCommentListByCursor(
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

        if (hasNext && !comments.isEmpty()) {
            nextCursor = comments.get(comments.size() - 1).getCommentId();
        }

        PaginationResponseDTO pagination = new PaginationResponseDTO(nextCursor, hasNext);

        return new GetCommentsResponseDTO(comments, pagination);
    }

    @Transactional
    public CommentUpdateResponseDTO updateComment(
            int commentId,
            int currentUserId,
            CommentUpdateRequestDTO request
    ) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.isWrittenBy(currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        comment.updateContent(request.getContent());

        return new CommentUpdateResponseDTO(commentId, request.getContent());
    }

    // 댓글 삭제와 posts.comment_count 감소가 하나의 작업으로 처리되도록 트랜잭션으로 묶는다.
    @Transactional
    public void deleteComment(int commentId, int currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.isWrittenBy(currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Integer postId = comment.getPost().getPostId();

        commentRepository.delete(comment);
        postRepository.decreaseCommentCount(postId);
    }
}
