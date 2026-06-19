package com.community.community.repository;

import com.community.community.entity.Post;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Integer>, PostRepositoryCustom {

    /*
     * count 컬럼은 Entity에서 값을 읽어 증가/감소시키면 동시 요청에서 lost update가 발생할 수 있다.
     * 그래서 DB가 현재 값을 기준으로 직접 계산하도록 JPQL update query로 처리한다.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.postId = :postId")
    int increaseLikeCount(@Param("postId") Integer postId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.postId = :postId AND p.likeCount > 0")
    int decreaseLikeCount(@Param("postId") Integer postId);

    @Query("SELECT p.likeCount FROM Post p WHERE p.postId = :postId")
    int findLikeCountByPostId(@Param("postId") Integer postId);

    // 같은 게시글의 좋아요 토글 요청을 순차적으로 처리하기 위해 쓰기 잠금으로 조회한다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.postId = :postId")
    Optional<Post> findByIdForUpdate(@Param("postId") Integer postId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.postId = :postId")
    int increaseCommentCount(@Param("postId") Integer postId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.postId = :postId AND p.commentCount > 0")
    int decreaseCommentCount(@Param("postId") Integer postId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.postId = :postId")
    int increaseViewCount(@Param("postId") Integer postId);

    @Query("SELECT p.viewCount FROM Post p WHERE p.postId = :postId")
    int findViewCountByPostId(@Param("postId") Integer postId);
}
