package com.community.community.repository;

import com.community.community.dto.PostListItemResponseDTO;

import java.util.List;

// QueryDSL로 구현할 메서드를 Custom Repository interface에 따로 선언
// PostRepository는 인터페이스라 내부에 따로 구현할 수 없고, 기존에 구현하고 있는 JpaRepository는 기본적인 CRUD만 제공하기 때문
// 이 interface를 PostrepositoryImpl에서 QueryDSL로 구현한 후
// PostRepository에서 PostRepositoryCustom을 상속받으면,
// Service에서는 PostRepository 하나로 기본 CRUD와 QueryDSL 커스텀 메서드를 모두 호출 가능
public interface PostRepositoryCustom {

    // page 목록 조회시 응답 DTO들을 반환할 메서드
    List<PostListItemResponseDTO> findPostListByCursor(int cursor, int limit);

    // 검색 결과 목록 조회시 응답 DTO들을 반환할 메서드
    List<PostListItemResponseDTO> searchPostList(
            String keyword,
            int offset,
            int limit,
            String sort
    );
}
