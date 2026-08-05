package com.community.community.repository;

import com.community.community.dto.PostAuthorResponseDTO;
import com.community.community.dto.PostListItemResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostSearchJdbcRepository {

    private static final int RECENT_SCAN_WINDOW = 5000;
    private static final int LIST_CONTENT_MAX_LENGTH = 140;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<PostListItemResponseDTO> searchRecentWithinWindow(
            String keyword,
            int limit
    ) {
        String sql = """
                SELECT
                    p.post_id,
                    p.title,
                    SUBSTRING(p.content, 1, :contentLength) AS content_preview,
                    p.image_url,
                    p.created_at,
                    p.like_count,
                    p.comment_count,
                    p.view_count,
                    u.user_id,
                    u.nickname,
                    u.profile_image_url
                FROM posts p
                JOIN users u
                  ON u.user_id = p.user_id
                WHERE p.post_id > (
                    SELECT MAX(latest.post_id)
                    FROM posts latest
                ) - :scanWindow
                  AND (
                      LOWER(p.title) LIKE LOWER(:likePattern) ESCAPE '!'
                      OR LOWER(p.content) LIKE LOWER(:likePattern) ESCAPE '!'
                      OR LOWER(u.nickname) LIKE LOWER(:likePattern) ESCAPE '!'
                  )
                ORDER BY p.post_id DESC
                LIMIT :limit
                """;

        MapSqlParameterSource parameters = commonParameters(keyword, limit)
                .addValue("scanWindow", RECENT_SCAN_WINDOW);

        return jdbcTemplate.query(sql, parameters, postRowMapper());
    }

    public List<PostListItemResponseDTO> searchRecentByFullText(
            String keyword,
            int limit
    ) {
        String sql = """
                WITH candidate_post_ids AS (
                    SELECT p.post_id
                    FROM posts p
                    WHERE MATCH(p.title, p.content)
                          AGAINST(:fullTextKeyword IN BOOLEAN MODE)

                    UNION DISTINCT

                    SELECT p.post_id
                    FROM users u
                    JOIN posts p
                      ON p.user_id = u.user_id
                    WHERE MATCH(u.nickname)
                          AGAINST(:fullTextKeyword IN BOOLEAN MODE)
                )
                SELECT
                    p.post_id,
                    p.title,
                    SUBSTRING(p.content, 1, :contentLength) AS content_preview,
                    p.image_url,
                    p.created_at,
                    p.like_count,
                    p.comment_count,
                    p.view_count,
                    u.user_id,
                    u.nickname,
                    u.profile_image_url
                FROM candidate_post_ids c
                JOIN posts p
                  ON p.post_id = c.post_id
                JOIN users u
                  ON u.user_id = p.user_id
                WHERE LOWER(p.title) LIKE LOWER(:likePattern) ESCAPE '!'
                   OR LOWER(p.content) LIKE LOWER(:likePattern) ESCAPE '!'
                   OR LOWER(u.nickname) LIKE LOWER(:likePattern) ESCAPE '!'
                ORDER BY p.post_id DESC
                LIMIT :limit
                """;

        MapSqlParameterSource parameters = commonParameters(keyword, limit)
                .addValue("fullTextKeyword", keyword);

        return jdbcTemplate.query(sql, parameters, postRowMapper());
    }

    private MapSqlParameterSource commonParameters(String keyword, int limit) {
        return new MapSqlParameterSource()
                .addValue("likePattern", toLikePattern(keyword))
                .addValue("contentLength", LIST_CONTENT_MAX_LENGTH)
                .addValue("limit", limit);
    }

    private String toLikePattern(String keyword) {
        String escapedKeyword = keyword
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");

        return "%" + escapedKeyword + "%";
    }

    private RowMapper<PostListItemResponseDTO> postRowMapper() {
        return (resultSet, rowNumber) -> {
            PostAuthorResponseDTO author = new PostAuthorResponseDTO(
                    resultSet.getInt("user_id"),
                    resultSet.getString("nickname"),
                    resultSet.getString("profile_image_url")
            );

            Timestamp createdAt = resultSet.getTimestamp("created_at");

            return new PostListItemResponseDTO(
                    resultSet.getInt("post_id"),
                    resultSet.getString("title"),
                    author,
                    resultSet.getString("content_preview"),
                    resultSet.getString("image_url"),
                    createdAt.toLocalDateTime().toString(),
                    resultSet.getInt("like_count"),
                    resultSet.getInt("comment_count"),
                    resultSet.getInt("view_count")
            );
        };
    }
}
