package com.community.community.service;

import com.community.community.dto.*;
import com.community.community.entity.Post;
import com.community.community.repository.PostLikeRepository;
import com.community.community.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public PostService(PostRepository postRepository, PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
    }

    // post 작성 메서드
    // 인증 후에 진행할 수 있는 메서드는 매개변수로 현재 user_id까지 받아야 함.
    public Post createPost(PostCreateRequestDTO request, int currentId) {
        String title = request.getTitle();
        String content = request.getContent();
        String imageUrl = request.getImageUrl();

        // 제목과 본문은 필수값이므로 누락되면 400 응답 대상
        if (title == null || title.isBlank()
                || content == null || content.isBlank()) {
            throw new IllegalArgumentException("invalid_create_post_request");
        }

        // 제목은 화면 정책상 최대 26자
        if (title.length() > 26) {
            throw new IllegalArgumentException("invalid_create_post_request");
        }

        int postId = postRepository.save(title, content, imageUrl, currentId);
        Post post = postRepository.findById(postId);

        if (post == null) {
            throw new IllegalStateException("post_not_found");
        }

        return post;
    }

    public Post getPost(int postId) {
        Post post = postRepository.findById(postId);

        if (post == null) {
            throw new IllegalStateException("post_not_found");
        }

        return post;
    }

    public GetPostsResponseDTO getPosts(String cursorValue, String sizeValue) {
        int cursor;
        int size;

        try {
            cursor = Integer.parseInt(cursorValue);
            size = Integer.parseInt(sizeValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid_posts_request");
        }

        if (cursor < 0 || size <= 0) {
            throw new IllegalArgumentException("invalid_posts_request");
        }

        // 화면에 보여줄 개수보다 하나 더 조회하여 다음 페이지 존재 여부를 판단한다.
        List<PostListItemResponseDTO> posts = postRepository.findPostsByCursor(cursor, size + 1);

        boolean hasNext = posts.size() > size;

        if (hasNext) {
            posts = posts.subList(0, size);
        }

        Integer nextCursor = null;

        // 다음 조회는 post_id < cursor 조건을 사용하므로 마지막으로 응답한 ID 자체를 전달한다.
        if (hasNext && !posts.isEmpty()) {
            nextCursor = posts.get(posts.size() - 1).getPostId();
        }

        PaginationResponseDTO pagination = new PaginationResponseDTO();
        pagination.setNextCursor(nextCursor);
        pagination.setHasNext(hasNext);

        GetPostsResponseDTO getPostsResponseDTO = new GetPostsResponseDTO();
        getPostsResponseDTO.setPosts(posts);
        getPostsResponseDTO.setPagination(pagination);

        return getPostsResponseDTO;
    }

    public PostUpdateResponseDTO updatePost(int postId, int currentUserId, PostUpdateRequestDTO request) {
        if (request.getTitle() == null || request.getTitle().isBlank()
                || request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("invalid_update_post_request");
        }

        if (request.getTitle().length() > 26) {
            throw new IllegalArgumentException("invalid_update_post_request");
        }

        Integer authorId = postRepository.findAuthorIdByPostId(postId);

        if (authorId == null) {
            throw new IllegalStateException("post_not_found");
        }

        if (authorId != currentUserId) {
            throw new SecurityException("forbidden");
        }

        postRepository.update(
                postId,
                request.getTitle(),
                request.getContent(),
                request.getImageUrl()
        );

        PostUpdateResponseDTO postUpdateResponseDTO = new PostUpdateResponseDTO();
        postUpdateResponseDTO.setPostId(postId);

        return postUpdateResponseDTO;
    }

    public void deletePost(int postId, int currentUserId) {
        Integer authorId = postRepository.findAuthorIdByPostId(postId);

        if (authorId == null) {
            throw new IllegalStateException("post_not_found");
        }

        if (authorId != currentUserId) {
            throw new SecurityException("forbidden");
        }

        postRepository.deleteById(postId);
    }

    // post_likes 변경과 posts.like_count 변경 중 하나만 반영되는 것을 막기 위해 트랜잭션으로 묶는다.
    @Transactional
    public PostLikeResponseDTO toggleLike(int postId, int currentUserId) {
        if (postRepository.findById(postId) == null) {
            throw new IllegalStateException("post_not_found");
        }

        boolean liked;

        // 좋아요 여부는 사용자마다 달라지는 값이므로 posts의 고정 컬럼으로 저장하지 않는다.
        // post_likes에 post_id와 user_id 조합이 있는지 확인하여 등록과 취소를 결정한다.
        if (postLikeRepository.exists(postId, currentUserId)) {
            postLikeRepository.delete(postId, currentUserId);
            postRepository.decreaseLikeCount(postId);
            liked = false;
        } else {
            postLikeRepository.save(postId, currentUserId);
            postRepository.increaseLikeCount(postId);
            liked = true;
        }

        int likeCount = postRepository.findLikeCountByPostId(postId);

        PostLikeResponseDTO response = new PostLikeResponseDTO();
        response.setPostId(postId);
        response.setLiked(liked);
        response.setLikeCount(likeCount);

        return response;
    }

}
