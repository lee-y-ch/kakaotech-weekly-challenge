package com.community.community.service;

import com.community.community.entity.Post;
import com.community.community.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectViewCountServiceTest {

    @Mock
    PostRepository postRepository;

    @Test
    void incrementsDatabaseWhenBufferIsDisabled() {
        Post post = new Post(null, "title", "content", null);
        ReflectionTestUtils.setField(post, "postId", 10);
        when(postRepository.findViewCountByPostId(10)).thenReturn(7);

        int result = new DirectViewCountService(postRepository).incrementAndGet(post);

        assertThat(result).isEqualTo(7);
        verify(postRepository).increaseViewCount(10);
    }
}
