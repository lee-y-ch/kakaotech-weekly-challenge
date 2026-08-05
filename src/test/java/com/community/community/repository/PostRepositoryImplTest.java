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

    @Test
    void relevanceFirstPageReusesProbeWhenWindowIsDense() {
        int limit = 13;
        PostListItemResponseDTO item =
                mock(PostListItemResponseDTO.class);

        List<PostListItemResponseDTO> probe =
                Collections.nCopies(51, item);

        when(postSearchJdbcRepository
                .searchRelevanceWithinWindow(
                        "한강",
                        0,
                        51
                ))
                .thenReturn(probe);

        List<PostListItemResponseDTO> result =
                postRepository.searchPostList(
                        "한강",
                        0,
                        limit,
                        "relevance"
                );

        assertThat(result)
                .hasSize(limit)
                .containsOnly(item);

        verify(postSearchJdbcRepository)
                .searchRelevanceWithinWindow(
                        "한강",
                        0,
                        51
                );

        verify(postSearchJdbcRepository, never())
                .searchRelevanceByFullText(
                        anyString(),
                        anyInt(),
                        anyInt()
                );

        verifyNoInteractions(queryFactory);
    }

    @Test
    void relevanceLaterPageKeepsBoundedStrategyWhenWindowIsDense() {
        int offset = 12;
        int limit = 13;

        PostListItemResponseDTO probeItem =
                mock(PostListItemResponseDTO.class);
        PostListItemResponseDTO pageItem =
                mock(PostListItemResponseDTO.class);

        List<PostListItemResponseDTO> probe =
                Collections.nCopies(51, probeItem);
        List<PostListItemResponseDTO> pageResult =
                Collections.nCopies(limit, pageItem);

        when(postSearchJdbcRepository
                .searchRelevanceWithinWindow(
                        "한강",
                        0,
                        51
                ))
                .thenReturn(probe);

        when(postSearchJdbcRepository
                .searchRelevanceWithinWindow(
                        "한강",
                        offset,
                        limit
                ))
                .thenReturn(pageResult);

        List<PostListItemResponseDTO> result =
                postRepository.searchPostList(
                        "한강",
                        offset,
                        limit,
                        "relevance"
                );

        assertThat(result).isSameAs(pageResult);

        verify(postSearchJdbcRepository)
                .searchRelevanceWithinWindow(
                        "한강",
                        0,
                        51
                );

        verify(postSearchJdbcRepository)
                .searchRelevanceWithinWindow(
                        "한강",
                        offset,
                        limit
                );

        verify(postSearchJdbcRepository, never())
                .searchRelevanceByFullText(
                        anyString(),
                        anyInt(),
                        anyInt()
                );

        verifyNoInteractions(queryFactory);
    }

    @Test
    void relevanceKeepsGlobalStrategyWhenWindowIsSparse() {
        int offset = 12;
        int limit = 13;

        PostListItemResponseDTO probeItem =
                mock(PostListItemResponseDTO.class);
        PostListItemResponseDTO globalItem =
                mock(PostListItemResponseDTO.class);

        List<PostListItemResponseDTO> probe =
                Collections.nCopies(2, probeItem);
        List<PostListItemResponseDTO> globalResult =
                Collections.nCopies(limit, globalItem);

        when(postSearchJdbcRepository
                .searchRelevanceWithinWindow(
                        "오로라",
                        0,
                        51
                ))
                .thenReturn(probe);

        when(postSearchJdbcRepository
                .searchRelevanceByFullText(
                        "오로라",
                        offset,
                        limit
                ))
                .thenReturn(globalResult);

        List<PostListItemResponseDTO> result =
                postRepository.searchPostList(
                        "오로라",
                        offset,
                        limit,
                        "relevance"
                );

        assertThat(result).isSameAs(globalResult);

        verify(postSearchJdbcRepository)
                .searchRelevanceWithinWindow(
                        "오로라",
                        0,
                        51
                );

        verify(postSearchJdbcRepository)
                .searchRelevanceByFullText(
                        "오로라",
                        offset,
                        limit
                );

        verify(postSearchJdbcRepository, never())
                .searchRelevanceWithinWindow(
                        "오로라",
                        offset,
                        limit
                );

        verifyNoInteractions(queryFactory);
    }
}
