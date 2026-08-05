package com.community.community.repository;

import com.community.community.dto.PostListItemResponseDTO;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostRepositoryImplTest {

    @Mock
    private JPAQueryFactory queryFactory;

    @Mock
    private PostSearchJdbcRepository postSearchJdbcRepository;

    @InjectMocks
    private PostRepositoryImpl postRepository;

    @Test
    void recentFirstPageReturnsFastPathWhenLimitIsFilled() {
        int limit = 13;
        PostListItemResponseDTO item = mock(PostListItemResponseDTO.class);
        List<PostListItemResponseDTO> fastPath =
                Collections.nCopies(limit, item);

        when(postSearchJdbcRepository.searchRecentWithinWindow("사진", limit))
                .thenReturn(fastPath);

        List<PostListItemResponseDTO> result =
                postRepository.searchPostList("사진", 0, limit, "recent");

        assertThat(result).isSameAs(fastPath);

        verify(postSearchJdbcRepository)
                .searchRecentWithinWindow("사진", limit);
        verify(postSearchJdbcRepository, never())
                .searchRecentByFullText(anyString(), anyInt());
        verifyNoInteractions(queryFactory);
    }

    @Test
    void recentFirstPageFallsBackToFullTextWhenWindowIsInsufficient() {
        int limit = 13;
        PostListItemResponseDTO item = mock(PostListItemResponseDTO.class);

        List<PostListItemResponseDTO> fastPath =
                Collections.nCopies(2, item);
        List<PostListItemResponseDTO> fullTextResult =
                Collections.nCopies(limit, item);

        when(postSearchJdbcRepository.searchRecentWithinWindow("오로라", limit))
                .thenReturn(fastPath);
        when(postSearchJdbcRepository.searchRecentByFullText("오로라", limit))
                .thenReturn(fullTextResult);

        List<PostListItemResponseDTO> result =
                postRepository.searchPostList("오로라", 0, limit, "recent");

        assertThat(result).isSameAs(fullTextResult);

        verify(postSearchJdbcRepository)
                .searchRecentWithinWindow("오로라", limit);
        verify(postSearchJdbcRepository)
                .searchRecentByFullText("오로라", limit);
        verifyNoInteractions(queryFactory);
    }
}
