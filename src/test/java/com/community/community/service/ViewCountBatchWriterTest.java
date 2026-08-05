package com.community.community.service;

import com.community.community.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ViewCountBatchWriterTest {

    @Mock
    PostRepository postRepository;

    @Test
    void appliesOneDatabaseUpdatePerPostDelta() {
        Map<Integer, Long> deltas = new LinkedHashMap<>();
        deltas.put(10, 120L);
        deltas.put(20, 3L);

        new ViewCountBatchWriter(postRepository).apply(deltas);

        verify(postRepository).increaseViewCountBy(10, 120L);
        verify(postRepository).increaseViewCountBy(20, 3L);
    }
}
