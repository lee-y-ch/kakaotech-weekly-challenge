package com.community.community.service;

import com.community.community.dto.PostUpdateRequestDTO;
import com.community.community.entity.Post;
import com.community.community.entity.User;
import com.community.community.exception.BusinessException;
import com.community.community.exception.ErrorCode;
import com.community.community.repository.PostLikeRepository;
import com.community.community.repository.PostRepository;
import com.community.community.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.View;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * PostService 단위 테스트
 * 게시글 수정/삭제의 작성자 권한 검증이 핵심
 * 파라미터 검증(페이지네이션)도 함께 확인
 */
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock PostRepository postRepository;
    @Mock PostLikeRepository postLikeRepository;
    @Mock UserRepository userRepository;
    @Mock ImageS3Service imageS3Service;
    @Mock View view;

    @InjectMocks PostService postService;

    // userId 는 @GeneratedValue 라 생성자로 지정할 수 없어 리플렉션으로 주입
    private User userWithId(int userId) {
        User user = new User("u" + userId + "@example.com", "pw", "nick" + userId, "http://img/p.png");
        ReflectionTestUtils.setField(user, "userId", userId);
        return user;
    }

    private Post postByAuthor(User author) {
        return new Post(author, "title", "content", "http://img/post.png");
    }

    private PostUpdateRequestDTO updateRequest() {
        PostUpdateRequestDTO dto = new PostUpdateRequestDTO();
        dto.setTitle("new title");
        dto.setContent("new content");
        dto.setImageUrl("http://img/new.png");
        return dto;
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 게시글을 수정하면 FORBIDDEN 예외가 발생한다")
    void updatePost_fail_whenNotAuthor() {
        // given: 게시글 작성자는 1번, 수정 시도자는 2번
        Post post = postByAuthor(userWithId(1));
        when(postRepository.findById(10)).thenReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.updatePost(10, 2, updateRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 수정하면 POST_NOT_FOUND 예외가 발생한다")
    void updatePost_fail_whenPostNotFound() {
        // given
        when(postRepository.findById(999)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.updatePost(999, 1, updateRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 게시글을 삭제하면 FORBIDDEN 예외가 발생한다")
    void deletePost_fail_whenNotAuthor() {
        // given
        Post post = postByAuthor(userWithId(1));
        when(postRepository.findById(10)).thenReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.deletePost(10, 2))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("페이지네이션 파라미터가 숫자가 아니면 INVALID_POSTS_REQUEST 예외가 발생한다")
    void getPosts_fail_whenParamNotNumber() {
        assertThatThrownBy(() -> postService.getPosts("abc", "10"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_POSTS_REQUEST);
    }

    @Test
    @DisplayName("페이지네이션 size 가 0 이하이면 INVALID_POSTS_REQUEST 예외가 발생한다")
    void getPosts_fail_whenSizeNotPositive() {
        assertThatThrownBy(() -> postService.getPosts("0", "0"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_POSTS_REQUEST);
    }
}
