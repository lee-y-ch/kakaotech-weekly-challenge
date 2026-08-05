package com.community.community.service;

import com.community.community.config.ViewCountBufferProperties;
import com.community.community.entity.Post;
import com.community.community.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeferredViewCountServiceTest {

    @Mock StringRedisTemplate redisTemplate;
    @Mock PostRepository postRepository;
    @Mock ViewCountBatchWriter batchWriter;

    private DeferredViewCountService service;
    private Post post;

    @BeforeEach
    void setUp() {
        ViewCountBufferProperties properties = new ViewCountBufferProperties(
                true,
                16,
                10_000,
                Duration.ofSeconds(30),
                Duration.ofHours(24)
        );

        service = new DeferredViewCountService(
                redisTemplate,
                postRepository,
                batchWriter,
                properties
        );

        post = new Post(null, "title", "content", null);
        ReflectionTestUtils.setField(post, "postId", 500001);
        ReflectionTestUtils.setField(post, "viewCount", 41);
    }

    @Test
    @SuppressWarnings("unchecked")
    void returnsValkeyTotalWithoutImmediateDatabaseUpdate() {
        when(redisTemplate.execute(
                any(RedisScript.class),
                anyList(),
                any(Object[].class)
        )).thenReturn(42L);

        int result = service.incrementAndGet(post);

        assertThat(result).isEqualTo(42);
        verify(postRepository, never()).increaseViewCount(anyInt());
        verify(postRepository, never()).findViewCountByPostId(anyInt());
    }

    @Test
    @SuppressWarnings("unchecked")
    void fallsBackToDatabaseWhenValkeyIsUnavailable() {
        when(redisTemplate.execute(
                any(RedisScript.class),
                anyList(),
                any(Object[].class)
        )).thenThrow(new RedisConnectionFailureException("unavailable"));
        when(postRepository.findViewCountByPostId(500001)).thenReturn(42);

        int result = service.incrementAndGet(post);

        assertThat(result).isEqualTo(42);
        verify(postRepository).increaseViewCount(500001);
        verify(postRepository).findViewCountByPostId(500001);
    }
}
