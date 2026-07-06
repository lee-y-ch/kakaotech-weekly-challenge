package com.community.community.repository;

import com.community.community.dto.PostAuthorResponseDTO;
import com.community.community.dto.PostListItemResponseDTO;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.community.community.entity.QPost.post;
import static com.community.community.entity.QUser.user;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /*
     * 게시글 목록 조회는 posts와 users를 함께 조회해야 한다.
     *
     * 기존 방식처럼 Post 엔티티를 먼저 조회한 뒤 post.getAuthor()에 접근하면,
     * author가 LAZY 로딩이기 때문에 게시글 개수만큼 추가 쿼리가 발생할 수 있다.
     *
     * 따라서 QueryDSL에서 posts와 users를 join하고,
     * 목록 화면에 필요한 컬럼만 선택해서 DTO로 변환한다.
     */
    @Override
    public List<PostListItemResponseDTO> findPostListByCursor(int cursor, int limit) {
        List<Tuple> tuples = queryFactory
                .select(
                        post.postId,
                        post.title,
                        post.content,
                        post.imageUrl,
                        post.createdAt,
                        post.likeCount,
                        post.commentCount,
                        post.viewCount,
                        user.userId,
                        user.nickname,
                        user.profileImageUrl
                )
                .from(post)
                .join(post.author, user)
                .where(cursorCondition(cursor))
                .orderBy(post.postId.desc())
                .limit(limit)
                .fetch();

        List<PostListItemResponseDTO> result = new ArrayList<>();

        for (Tuple tuple : tuples) {
            PostAuthorResponseDTO author = new PostAuthorResponseDTO(
                    tuple.get(user.userId),
                    tuple.get(user.nickname),
                    tuple.get(user.profileImageUrl)
            );

            PostListItemResponseDTO item = new PostListItemResponseDTO(
                    tuple.get(post.postId),
                    tuple.get(post.title),
                    author,
                    tuple.get(post.content),
                    tuple.get(post.imageUrl),
                    tuple.get(post.createdAt).toString(),
                    tuple.get(post.likeCount),
                    tuple.get(post.commentCount),
                    tuple.get(post.viewCount)
            );

            result.add(item);
        }

        return result;
    }

    /*
     * 첫 조회에서는 cursor가 0이므로 별도 조건을 걸지 않는다.
     * 추가 조회에서는 마지막으로 받은 post_id보다 작은 게시글만 조회한다.
     *
     * QueryDSL where()는 null 조건을 무시하므로,
     * cursor가 0일 때 null을 반환하면 조건 없이 전체 최신 목록을 조회할 수 있다.
     */
    private BooleanExpression cursorCondition(int cursor) {
        if (cursor == 0) {
            return null;
        }

        return post.postId.lt(cursor);
    }
}