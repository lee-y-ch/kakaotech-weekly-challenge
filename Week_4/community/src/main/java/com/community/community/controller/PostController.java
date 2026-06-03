package com.community.community.controller;

import com.community.community.ApiResponse;
import com.community.community.dto.*;
import com.community.community.entity.Post;
import com.community.community.service.AuthService;
import com.community.community.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PostController {

    private final PostService postService;
    private final AuthService authService;

    public PostController(PostService postService, AuthService authService) {
        this.postService = postService;
        this.authService = authService;
    }

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(
            // Header 누락도 인증 실패로 처리하기 위해 required = false로 설정한다.
            // 기본값(true)을 사용하면 Controller 진입 전에 Spring이 400을 반환할 수 있다.
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody PostCreateRequestDTO request
    ) {
        try {
            int currentUserId = authService.getCurrentUserId(authorization);

            Post post = postService.createPost(request, currentUserId);

            PostResponseDTO postResponseDTO = new PostResponseDTO();
            postResponseDTO.setPostId(post.getPostId());
            postResponseDTO.setTitle(post.getTitle());
            postResponseDTO.setContent(post.getContent());
            postResponseDTO.setCreatedAt(post.getCreatedAt());
            postResponseDTO.setImageUrl(post.getImageUrl());
            postResponseDTO.setViewCount(post.getViewCount());
            postResponseDTO.setLikeCount(post.getLikeCount());
            postResponseDTO.setCommentCount(post.getCommentCount());

            CreatePostResponseDTO data = new CreatePostResponseDTO(postResponseDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ApiResponse<>("create_post_success", data)
            );

            // 제목/본문 누락, 제목 길이 초과 등 잘못된 게시글 작성 요청에 대한 400 응답
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // session_id가 없거나 유효하지 않은 경우에 대한 401 응답
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // 저장 후 게시글을 다시 조회하지 못한 경우에 대한 404 응답
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // 예상하지 못한 서버 내부 오류에 대한 500 응답
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("internal_server_error", null)
            );
        }
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<?> getPost(
            @PathVariable int postId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        try {
            // 인증 확인. 상세 조회는 작성자 권한까지는 필요 없으므로 userId를 비교하지 않는다.
            authService.getCurrentUserId(authorization);

            Post post = postService.getPost(postId);

            PostResponseDTO postResponseDTO = new PostResponseDTO();
            postResponseDTO.setPostId(post.getPostId());
            postResponseDTO.setTitle(post.getTitle());
            postResponseDTO.setContent(post.getContent());
            postResponseDTO.setCreatedAt(post.getCreatedAt());
            postResponseDTO.setImageUrl(post.getImageUrl());
            postResponseDTO.setViewCount(post.getViewCount());
            postResponseDTO.setLikeCount(post.getLikeCount());
            postResponseDTO.setCommentCount(post.getCommentCount());

            GetPostResponseDTO data = new GetPostResponseDTO(postResponseDTO);

            return ResponseEntity.ok(
                    new ApiResponse<>("get_post_success", data)
            );
            // post_id 형식 오류 등 잘못된 게시글 조회 요청에 대한 400 응답
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // session_id가 없거나 유효하지 않은 경우에 대한 401 응답
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // post_id에 해당하는 게시글이 없는 경우에 대한 404 응답
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // 예상하지 못한 서버 내부 오류에 대한 500 응답
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("internal_server_error", null)
            );
        }

    }

    @GetMapping("/posts")
    public ResponseEntity<?> getPosts(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "0") String cursor,
            @RequestParam(defaultValue = "10") String size
    ) {
        try {
            authService.getCurrentUserId(authorization);

            GetPostsResponseDTO data = postService.getPosts(cursor, size);

            return ResponseEntity.ok(
                    new ApiResponse<>("get_posts_success", data)
            );

            // cursor, size 값이 숫자가 아니거나 잘못된 범위인 경우: 400
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // session_id가 없거나 만료되어 인증에 실패한 경우: 401
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("unauthorized", null)
            );

            // 서버 내부 오류: 500
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("internal_server_error", null)
            );
        }
    }

    @PatchMapping("/posts/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable int postId,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody PostUpdateRequestDTO request
    ) {
        try {
            int currentUserId = authService.getCurrentUserId(authorization);

            PostUpdateResponseDTO data = postService.updatePost(postId, currentUserId, request);

            return ResponseEntity.ok(
                    new ApiResponse<>("update_post_success", data)
            );

            // title, content 누락 또는 제목 길이 초과 등 요청값이 잘못된 경우: 400
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // session_id가 없거나 만료되어 인증에 실패한 경우: 401
        } catch (SecurityException e) {
            if ("forbidden".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ApiResponse<>("forbidden", null)
                );
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("unauthorized", null)
            );

            // 수정하려는 게시글이 존재하지 않는 경우: 404
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

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable int postId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        try {
            int currentUserId = authService.getCurrentUserId(authorization);

            postService.deletePost(postId, currentUserId);

            return ResponseEntity.noContent().build();

            // session_id가 없거나 만료되어 인증에 실패한 경우: 401
        } catch (SecurityException e) {
            if ("forbidden".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ApiResponse<>("forbidden", null)
                );
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("unauthorized", null)
            );

            // 삭제하려는 게시글이 존재하지 않는 경우: 404
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

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable int postId,

            // Header 누락 시 Spring의 기본 400 응답 대신 직접 401 응답을 반환하기 위해 false로 설정한다.
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        try {
            int currentUserId = authService.getCurrentUserId(authorization);

            PostLikeResponseDTO data = postService.toggleLike(
                    postId,
                    currentUserId
            );

            return ResponseEntity.ok(
                    new ApiResponse<>("toggle_post_like_success", data)
            );

            // session_id가 없거나 만료된 경우: 401
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("unauthorized", null)
            );

            // 좋아요를 누르려는 게시글이 존재하지 않는 경우: 404
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
