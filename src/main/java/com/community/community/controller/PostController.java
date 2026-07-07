package com.community.community.controller;

import com.community.community.ApiResponse;
import com.community.community.auth.CurrentUserId;
import com.community.community.dto.*;
import com.community.community.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(
            // accessToken 쿠키가 없어도 Controller에서 직접 401 응답을 만들기 위해 required = false로 받는다.
            // required = true이면 쿠키 누락 시 Spring이 먼저 400을 반환할 수 있다.
            @CurrentUserId int currentUserId,
            @Valid @RequestBody PostCreateRequestDTO request
    ) {
        CreatePostResponseDTO data = postService.createPost(currentUserId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("create_post_success", data));
    }

    @GetMapping("/posts/search")
    public ResponseEntity<?> searchPosts(
            @CurrentUserId int currentUserId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") String cursor,
            @RequestParam(defaultValue = "10") String size,
            @RequestParam(defaultValue = "recent") String sort
    ) {
        GetPostsResponseDTO data = postService.searchPosts(
                keyword,
                cursor,
                size,
                sort
        );

        return ResponseEntity.ok(
                new ApiResponse<>("search_posts_success", data)
        );
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<?> getPost(
            @PathVariable int postId,
            @CurrentUserId int currentUserId

    ) {
        GetPostResponseDTO data = postService.getPost(postId, currentUserId);

        return ResponseEntity.ok(
                new ApiResponse<>("get_post_success", data)
        );
    }

    @GetMapping("/posts")
    public ResponseEntity<?> getPosts(
            @CurrentUserId int currentUserId,
            @RequestParam(defaultValue = "0") String cursor,
            @RequestParam(defaultValue = "10") String size
    ) {
        GetPostsResponseDTO data = postService.getPosts(cursor, size);

        return ResponseEntity.ok(
                new ApiResponse<>("get_posts_success", data)
        );
    }

    @PatchMapping("/posts/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable int postId,
            @CurrentUserId int currentUserId,
            @Valid @RequestBody PostUpdateRequestDTO request
    ) {
        PostUpdateResponseDTO data = postService.updatePost(postId, currentUserId, request);

        return ResponseEntity.ok(
                new ApiResponse<>("update_post_success", data)
        );
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable int postId,
            @CurrentUserId int currentUserId
    ) {
        postService.deletePost(postId, currentUserId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable int postId,
            @CurrentUserId int currentUserId
    ) {
        PostLikeResponseDTO data = postService.toggleLike(
                postId,
                currentUserId
        );

        return ResponseEntity.ok(
                new ApiResponse<>("toggle_post_like_success", data)
        );
    }
}
