package com.community.community.service;

import com.community.community.dto.*;
import com.community.community.entity.Post;
import com.community.community.entity.PostLike;
import com.community.community.entity.PostLikeId;
import com.community.community.entity.User;
import com.community.community.exception.BusinessException;
import com.community.community.exception.ErrorCode;
import com.community.community.repository.PostLikeRepository;
import com.community.community.repository.PostRepository;
import com.community.community.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.View;

import java.util.List;
import java.util.Objects;

@Service
public class PostService {

    private static final int MIN_SEARCH_KEYWORD_LENGTH = 2;
    private static final int MAX_SEARCH_LIMIT = 50;

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final ImageS3Service imageS3Service;
    private final View view;

    public PostService(
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            UserRepository userRepository,
            View view,
            ImageS3Service imageS3Service) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.userRepository = userRepository;
        this.view = view;
        this.imageS3Service = imageS3Service;
    }

    // post 작성 메서드
    // 인증 후에 진행할 수 있는 메서드는 매개변수로 현재 user_id까지 받아야 함.
    @Transactional
    public CreatePostResponseDTO createPost(int currentUserId, PostCreateRequestDTO request) {

        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        Post post = new Post(
                author,
                request.getTitle(),
                request.getContent(),
                request.getImageUrl()
        );

        Post savedPost = postRepository.save(post);

        CreatedPostResponseDTO createdPost = new CreatedPostResponseDTO(
                savedPost.getPostId()
        );

        return new CreatePostResponseDTO(createdPost);
    }

    @Transactional
    public GetPostResponseDTO getPost(int postId, int currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        User author = post.getAuthor();

        PostAuthorResponseDTO authorResponse = new PostAuthorResponseDTO(
                author.getUserId(),
                author.getNickname(),
                author.getProfileImageUrl()
        );

        boolean isLiked = postLikeRepository.existsById(
                new PostLikeId(postId, currentUserId)
        );

        boolean isAuthor = author.getUserId().equals(currentUserId);

        // JPQL update query는 이미 조회한 post 객체의 viewCount를 갱신하지 않으므로 최신 값을 다시 조회한다.
        postRepository.increaseViewCount(postId);
        int viewCount = postRepository.findViewCountByPostId(postId);

        PostResponseDTO postResponse = new PostResponseDTO(
                post.getPostId(),
                post.getTitle(),
                post.getContent(),
                post.getImageUrl(),
                authorResponse,
                post.getCreatedAt().toString(),
                post.getLikeCount(),
                post.getCommentCount(),
                viewCount,
                isLiked,
                isAuthor
        );

        return new GetPostResponseDTO(postResponse);
    }

    public GetPostsResponseDTO getPosts(String cursorValue, String sizeValue) {
        int cursor;
        int size;

        try {
            cursor = Integer.parseInt(cursorValue);
            size = Integer.parseInt(sizeValue);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_POSTS_REQUEST);
        }

        if (cursor < 0 || size <= 0) {
            throw new BusinessException(ErrorCode.INVALID_POSTS_REQUEST);
        }

        /*
         * 다음 페이지가 있는지 확인하기 위해 요청 size보다 1개 더 조회한다.
         * 예를 들어 화면에는 10개만 보여주지만 DB에서는 11개를 조회한다.
         * 11번째 데이터가 있으면 has_next=true로 판단한다.
         */
        List<PostListItemResponseDTO> posts = postRepository.findPostListByCursor(
                cursor,
                size + 1
        );

        boolean hasNext = posts.size() > size;

        if (hasNext) {
            posts = posts.subList(0, size);
        }

        Integer nextCursor = null;

        if (hasNext && !posts.isEmpty()) {
            nextCursor = posts.get(posts.size() - 1).getPostId();
        }

        PaginationResponseDTO pagination = new PaginationResponseDTO(nextCursor, hasNext);

        return new GetPostsResponseDTO(posts, pagination);
    }

    public GetPostsResponseDTO searchPosts(
            String keywordValue,
            String cursorValue,
            String sizeValue,
            String sortValue
    ) {
        String keyword = keywordValue == null ? "" : keywordValue.trim();

        if (keyword.length() < MIN_SEARCH_KEYWORD_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_POSTS_REQUEST);
        }

        int offset;
        int limit;

        try {
            offset = Integer.parseInt(cursorValue);
            limit = Integer.parseInt(sizeValue);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_POSTS_REQUEST);
        }

        if (offset < 0 || limit <= 0 || limit > MAX_SEARCH_LIMIT) {
            throw new BusinessException(ErrorCode.INVALID_POSTS_REQUEST);
        }

        String sort = "relevance".equalsIgnoreCase(sortValue)
                ? "relevance"
                : "recent";

        List<PostListItemResponseDTO> posts = postRepository.searchPostList(
                keyword,
                offset,
                limit + 1,
                sort
        );

        boolean hasNext = posts.size() > limit;

        if (hasNext) {
            posts = posts.subList(0, limit);
        }

        Integer nextCursor = hasNext ? offset + limit : null;
        PaginationResponseDTO pagination = new PaginationResponseDTO(nextCursor, hasNext);

        return new GetPostsResponseDTO(posts, pagination);
    }

    @Transactional
    public PostUpdateResponseDTO updatePost(int postId, int currentUserId, PostUpdateRequestDTO request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (!post.getAuthor().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        String previousImageUrl = post.getImageUrl();
        // JPA 변경 감지를 사용하기 위해 영속 상태의 Post 엔티티 값만 변경한다.

        post.update(
                request.getTitle(),
                request.getContent(),
                request.getImageUrl()
        );

        if (!Objects.equals(previousImageUrl, request.getImageUrl())) {
            imageS3Service.deleteImageByUrlAfterCommit(previousImageUrl);
        }

        return new PostUpdateResponseDTO(postId);
    }

    public void deletePost(int postId, int currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (!post.getAuthor().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        String imageUrl = post.getImageUrl();

        postRepository.delete(post);

        imageS3Service.deleteImageByUrlAfterCommit(imageUrl);
    }

    // post_likes 변경과 posts.like_count 변경이 하나의 작업으로 처리되도록 트랜잭션으로 묶는다.
    @Transactional
    public PostLikeResponseDTO toggleLike(int postId, int currentUserId) {
        Post post = postRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        PostLikeId postLikeId = new PostLikeId(postId, currentUserId);

        boolean liked;

        if (postLikeRepository.existsById(postLikeId)) {
            PostLike postLike = postLikeRepository.findById(postLikeId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.POST_LIKE_NOT_FOUND));

            postLikeRepository.delete(postLike);
            postRepository.decreaseLikeCount(postId);
            liked = false;
        } else {
            PostLike postLike = new PostLike(post, user);

            postLikeRepository.save(postLike);
            postRepository.increaseLikeCount(postId);
            liked = true;
        }

        int likeCount = postRepository.findLikeCountByPostId(postId);

        return new PostLikeResponseDTO(
                postId,
                liked,
                likeCount
        );
    }

}
