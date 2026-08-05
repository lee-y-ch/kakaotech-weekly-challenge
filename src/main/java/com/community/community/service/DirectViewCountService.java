package com.community.community.service;

import com.community.community.entity.Post;
import com.community.community.repository.PostRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(
        name = "view-count.buffer.enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class DirectViewCountService implements ViewCountService {

    private final PostRepository postRepository;

    public DirectViewCountService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    @Transactional
    public int incrementAndGet(Post post) {
        postRepository.increaseViewCount(post.getPostId());
        return postRepository.findViewCountByPostId(post.getPostId());
    }
}
