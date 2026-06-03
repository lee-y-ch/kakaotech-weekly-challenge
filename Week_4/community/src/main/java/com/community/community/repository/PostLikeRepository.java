package com.community.community.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PostLikeRepository {
    private final JdbcTemplate jdbcTemplate;

    public PostLikeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean exists(int postId, int userId) {
        String sql = """
                SELECT COUNT(*)
                FROM post_likes
                WHERE post_id = ?
                  AND user_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                postId,
                userId
        );

        return count != null && count > 0;
    }

    public void save(int postId, int userId) {
        String sql = """
                INSERT INTO post_likes (post_id, user_id)
                VALUES (?, ?)
                """;

        jdbcTemplate.update(sql, postId, userId);
    }

    public void delete(int postId, int userId) {
        String sql = """
                DELETE FROM post_likes
                WHERE post_id = ?
                  AND user_id = ?
                """;

        jdbcTemplate.update(sql, postId, userId);
    }
}
