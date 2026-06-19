package com.community.community.controller;

import com.community.community.ApiResponse;
import com.community.community.auth.CurrentUserId;
import com.community.community.dto.*;
import com.community.community.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> createComment(
            @PathVariable int postId,
            @CurrentUserId int currentUserId,
            @Valid @RequestBody CommentCreateRequestDTO request
    ) {
        CommentCreateResponseDTO data = commentService.createComment(
                postId,
                currentUserId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("create_comment_success", data)
        );
    }

    // accessToken 쿠키가 없어도 Controller 내부에서 직접 인증 실패를 처리할 수 있도록
    // required = false로 설정한다. 쿠키가 없으면 accessToken에 null이 전달되고,
    // AuthService에서 이를 확인하여 401 Unauthorized를 반환한다.
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<?> getComments(
            @PathVariable int postId,
            @CurrentUserId int currentUserId,
            @RequestParam(defaultValue = "0") String cursor,
            @RequestParam(defaultValue = "10") String size
    ) {
        GetCommentsResponseDTO data = commentService.getComments(
                postId,
                cursor,
                size,
                currentUserId
        );

        return ResponseEntity.ok(
                new ApiResponse<>("get_comments_success", data)
        );
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable int commentId,
            @CurrentUserId int currentUserId,
            @Valid @RequestBody CommentUpdateRequestDTO request
    ) {
        CommentUpdateResponseDTO data = commentService.updateComment(
                commentId,
                currentUserId,
                request
        );

        return ResponseEntity.ok(
                new ApiResponse<>("update_comment_success", data)
        );
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable int commentId,
            @CurrentUserId int currentUserId
    ) {
        commentService.deleteComment(commentId, currentUserId);

        return ResponseEntity.noContent().build();
    }
}
