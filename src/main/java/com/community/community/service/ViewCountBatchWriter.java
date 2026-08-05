package com.community.community.service;

import com.community.community.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
public class ViewCountBatchWriter {

    private static final Logger log = LoggerFactory.getLogger(ViewCountBatchWriter.class);

    private final PostRepository postRepository;

    public ViewCountBatchWriter(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional
    public void apply(Map<Integer, Long> deltas) {
        for (Map.Entry<Integer, Long> entry : deltas.entrySet()) {
            int updatedRows = postRepository.increaseViewCountBy(
                    entry.getKey(),
                    entry.getValue()
            );
            if (updatedRows == 0) {
                log.info(
                        "Skipping buffered view count for deleted post: postId={}, delta={}",
                        entry.getKey(),
                        entry.getValue()
                );
            }
        }
    }
}
