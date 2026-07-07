package com.community.community.repository;

import com.community.community.dto.CommentAuthorResponseDTO;
import com.community.community.dto.CommentListItemResponseDTO;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.community.community.entity.QComment.comment;
import static com.community.community.entity.QUser.user;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /*
     * 댓글 목록 조회는 comments와 users를 함께 조회해야 한다.
     *
     * 기존 방식처럼 Comment 엔티티를 먼저 조회하고 comment.getWriter()에 접근하면
     * writer가 LAZY 로딩이기 때문에 댓글 개수만큼 추가 쿼리가 발생할 수 있다.
     *
     * 따라서 QueryDSL에서 comments와 users를 join하고,
     * 댓글 목록 화면에 필요한 컬럼만 선택해서 DTO로 변환한다.
     */
    @Override
    public List<CommentListItemResponseDTO> findCommentListByCursor(
            int postId,
            int cursor,
            int limit,
            int currentUserId
    ) {
        List<Tuple> tuples = queryFactory
                .select(
                        comment.commentId,
                        comment.content,
                        comment.createdAt,
                        user.userId,
                        user.nickname,
                        user.profileImageUrl
                )
                .from(comment)
                .join(comment.writer, user)
                .where(
                        comment.post.postId.eq(postId),
                        cursorCondition(cursor)
                )
                .orderBy(comment.commentId.desc())
                .limit(limit)
                .fetch();

        List<CommentListItemResponseDTO> result = new ArrayList<>();

        for (Tuple tuple : tuples) {
            Integer writerId = tuple.get(user.userId);

            CommentAuthorResponseDTO author = new CommentAuthorResponseDTO(
                    writerId,
                    tuple.get(user.nickname),
                    tuple.get(user.profileImageUrl)
            );

            CommentListItemResponseDTO item = new CommentListItemResponseDTO(
                    tuple.get(comment.commentId),
                    tuple.get(comment.content),
                    tuple.get(comment.createdAt).toString(),
                    writerId.equals(currentUserId),
                    author
            );

            result.add(item);
        }

        return result;
    }

    /*
     * 첫 조회에서는 cursor가 0이므로 cursor 조건을 적용하지 않는다.
     * 추가 조회에서는 마지막으로 받은 comment_id보다 작은 댓글만 조회한다.
     *
     * QueryDSL where()는 null 조건을 무시하므로,
     * cursor가 0일 때 null을 반환하면 첫 조회와 추가 조회를 하나의 쿼리 구조로 처리할 수 있다.
     */
    private BooleanExpression cursorCondition(int cursor) {
        if (cursor == 0) {
            return null;
        }

        return comment.commentId.lt(cursor);
    }
}
