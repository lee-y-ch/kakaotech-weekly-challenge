package com.community.community.repository;

import com.community.community.dto.CommentAuthorResponseDTO;
import com.community.community.dto.CommentListItemResponseDTO;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CommentRepository {

    private final JdbcTemplate jdbcTemplate;

    public CommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int save(int postId, int userId, String content) {
        String sql = """
            INSERT INTO comments (post_id, user_id, content)
            VALUES (?, ?, ?)
            """;

        jdbcTemplate.update(sql, postId, userId, content);

        Integer commentId = jdbcTemplate.queryForObject(
                "SELECT LAST_INSERT_ID()",
                Integer.class
        );

        return commentId;
    }

    public List<CommentListItemResponseDTO> findCommentsByCursor(
            int postId,
            int cursor,
            int limit,
            int currentUserId
    ) {
        String sql = """
            SELECT c.comment_id,
                   c.content,
                   c.created_at,
                   c.user_id,
                   u.nickname,
                   u.profile_image
            FROM comments c
            JOIN users u ON c.user_id = u.user_id
            WHERE c.post_id = ?
              AND (? = 0 OR c.comment_id < ?)
            ORDER BY c.comment_id DESC
            LIMIT ?
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            CommentAuthorResponseDTO author = new CommentAuthorResponseDTO();

            author.setUserId(rs.getInt("user_id"));
            author.setNickname(rs.getString("nickname"));
            author.setProfileImage(rs.getString("profile_image"));

            CommentListItemResponseDTO comment = new CommentListItemResponseDTO();

            comment.setCommentId(rs.getInt("comment_id"));
            comment.setContent(rs.getString("content"));
            comment.setCreatedAt(rs.getString("created_at"));
            comment.setIsAuthor(rs.getInt("user_id") == currentUserId);
            comment.setAuthor(author);

            return comment;

        }, postId, cursor, cursor, limit);
    }

    public Integer findAuthorIdByCommentId(int commentId) {
        String sql = """
            SELECT user_id
            FROM comments
            WHERE comment_id = ?
            """;

        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, commentId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void updateContent(int commentId, String content) {
        String sql = """
            UPDATE comments
            SET content = ?
            WHERE comment_id = ?
            """;

        jdbcTemplate.update(sql, content, commentId);
    }

    public Integer findPostIdByCommentId(int commentId) {
        String sql = """
            SELECT post_id
            FROM comments
            WHERE comment_id = ?
            """;

        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, commentId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // 1: 정상적으로 댓글 하나 삭제
    // 0: 해당 댓글이 이미 없어서 삭제하지 못함
    public int deleteById(int commentId) {
        String sql = """
            DELETE FROM comments
            WHERE comment_id = ?
            """;

        return jdbcTemplate.update(sql, commentId);
    }


}
