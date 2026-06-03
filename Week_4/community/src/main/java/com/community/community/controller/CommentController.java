package com.community.community.controller;

import com.community.community.ApiResponse;
import com.community.community.dto.*;
import com.community.community.service.AuthService;
import com.community.community.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommentController {

    private final AuthService authService;
    private final CommentService commentService;

    public CommentController(AuthService authService, CommentService commentService) {
        this.authService = authService;
        this.commentService = commentService;
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> createComment(
            @PathVariable int postId,
            // Header 누락도 인증 실패로 처리하기 위해 required = false로 설정한다.
            // 기본값(true)을 사용하면 Controller 진입 전에 Spring이 400을 반환할 수 있다.
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody CommentCreateRequestDTO request
    ) {
        try {
            int currentUserId = authService.getCurrentUserId(authorization);

            CommentCreateResponseDTO data = commentService.createComment(
                    postId,
                    currentUserId,
                    request
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ApiResponse<>("create_comment_success", data)
            );

            // 댓글 내용이 비어 있는 등 요청값이 잘못된 경우: 422
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // session_id가 없거나 만료되어 인증에 실패한 경우: 401
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("unauthorized", null)
            );

            // 댓글을 작성하려는 게시글이 존재하지 않는 경우: 404
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // 서버 내부 오류: 500
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("internal_server_error", null)
            );
        }
    }

    // Authorization 헤더가 없어도 Controller 내부에서 직접 인증 실패를 처리할 수 있도록
    // required = false로 설정한다.
    // 헤더가 없으면 authorization에 null이 전달되고,
    // AuthService에서 이를 확인하여 401 Unauthorized를 반환한다.
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<?> getComments(
            @PathVariable int postId,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "0") String cursor,
            @RequestParam(defaultValue = "10") String size
    ) {
        try {
            int currentUserId = authService.getCurrentUserId(authorization);

            GetCommentsResponseDTO data = commentService.getComments(
                    postId,
                    cursor,
                    size,
                    currentUserId
            );

            return ResponseEntity.ok(
                    new ApiResponse<>("get_comments_success", data)
            );

            // cursor, size가 숫자가 아니거나 허용 범위를 벗어난 경우: 400
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // session_id가 없거나 만료되어 인증에 실패한 경우: 401
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("unauthorized", null)
            );

            // 댓글을 조회하려는 게시글이 존재하지 않는 경우: 404
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // 서버 내부 오류: 500
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("internal_server_error", null)
            );
        }
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable int commentId,

            // Header 누락 시 Spring의 기본 400 응답 대신 직접 401 응답을 반환하기 위해 false로 설정한다.
            @RequestHeader(value = "Authorization", required = false) String authorization,

            @RequestBody CommentUpdateRequestDTO request
    ) {
        try {
            int currentUserId = authService.getCurrentUserId(authorization);

            CommentUpdateResponseDTO data = commentService.updateComment(
                    commentId,
                    currentUserId,
                    request
            );

            return ResponseEntity.ok(
                    new ApiResponse<>("update_comment_success", data)
            );

            // content가 없거나 공백으로만 구성된 경우: 422
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // session_id가 없거나 만료된 경우: 401
            // 로그인했지만 댓글 작성자가 아닌 경우: 403
        } catch (SecurityException e) {
            if ("forbidden".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ApiResponse<>("forbidden", null)
                );
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("unauthorized", null)
            );

            // 수정하려는 댓글이 존재하지 않는 경우: 404
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // 서버 내부 오류: 500
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("internal_server_error", null)
            );
        }
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable int commentId,

            // Header 누락 시 Spring의 기본 400 응답 대신 직접 401 응답을 반환하기 위해 false로 설정한다.
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        try {
            int currentUserId = authService.getCurrentUserId(authorization);

            commentService.deleteComment(commentId, currentUserId);

            return ResponseEntity.noContent().build();

            // session_id가 없거나 만료된 경우: 401
            // 로그인했지만 댓글 작성자가 아닌 경우: 403
        } catch (SecurityException e) {
            if ("forbidden".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ApiResponse<>("forbidden", null)
                );
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("unauthorized", null)
            );

            // 삭제하려는 댓글이 존재하지 않는 경우: 404
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // 서버 내부 오류: 500
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("internal_server_error", null)
            );
        }
    }


}
