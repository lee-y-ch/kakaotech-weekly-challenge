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
                WITH recent_boundary AS (
                    SELECT COALESCE(
                        (
                            SELECT boundary.post_id
                            FROM posts boundary
                            ORDER BY boundary.post_id DESC
                            LIMIT 1 OFFSET :boundaryOffset
                        ),
                        0
                    ) AS min_post_id
                )
                SELECT
                    p.post_id,
                    p.title,
                    SUBSTRING(
                        p.content,
                        1,
                        :contentLength
                    ) AS content_preview,
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
                CROSS JOIN recent_boundary rb
                WHERE p.post_id >= rb.min_post_id
                  AND (
                      p.title LIKE :likePattern ESCAPE '!'
                      OR p.content LIKE :likePattern ESCAPE '!'
                      OR u.nickname LIKE :likePattern ESCAPE '!'
                  )
                ORDER BY p.post_id DESC
                LIMIT :limit
                """;

        MapSqlParameterSource parameters =
                commonParameters(keyword, limit)
                        .addValue(
                                "boundaryOffset",
                                RECENT_SCAN_WINDOW - 1
                        );

        return jdbcTemplate.query(
                sql,
                parameters,
                postRowMapper()
        );
    }

    public List<PostListItemResponseDTO> searchRecentByFullText(
            String keyword,
            int limit
    ) {
        String sql = """
                WITH candidate_post_ids AS (
                    SELECT
                        p.post_id
                    FROM posts p
                    WHERE MATCH(p.title, p.content)
                          AGAINST(
                              :fullTextKeyword IN BOOLEAN MODE
                          )

                    UNION DISTINCT

                    SELECT
                        p.post_id
                    FROM users u
                    JOIN posts p
                      ON p.user_id = u.user_id
                    WHERE MATCH(u.nickname)
                          AGAINST(
                              :fullTextKeyword IN BOOLEAN MODE
                          )
                )
                SELECT
                    p.post_id,
                    p.title,
                    SUBSTRING(
                        p.content,
                        1,
                        :contentLength
                    ) AS content_preview,
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
                WHERE p.title LIKE :likePattern ESCAPE '!'
                   OR p.content LIKE :likePattern ESCAPE '!'
                   OR u.nickname LIKE :likePattern ESCAPE '!'
                ORDER BY p.post_id DESC
                LIMIT :limit
                """;

        MapSqlParameterSource parameters =
                commonParameters(keyword, limit)
                        .addValue(
                                "fullTextKeyword",
                                keyword
                        );

        return jdbcTemplate.query(
                sql,
                parameters,
                postRowMapper()
        );
    }

    public List<PostListItemResponseDTO> searchRelevanceWithinWindow(
            String keyword,
            int offset,
            int limit
    ) {
        String sql = """
            WITH recent_boundary AS (
                SELECT COALESCE(
                    (
                        SELECT boundary.post_id
                        FROM posts boundary
                        ORDER BY boundary.post_id DESC
                        LIMIT 1 OFFSET :boundaryOffset
                    ),
                    0
                ) AS min_post_id
            ),
            post_candidates AS (
                SELECT
                    p.post_id,
                    CASE
                        WHEN p.title
                             LIKE :likePattern ESCAPE '!'
                            THEN 3
                        ELSE 2
                    END AS field_priority,
                    (
                        CHAR_LENGTH(p.title)
                        - CHAR_LENGTH(
                            REPLACE(
                                p.title,
                                :keyword,
                                ''
                            )
                        )
                        + CHAR_LENGTH(p.content)
                        - CHAR_LENGTH(
                            REPLACE(
                                p.content,
                                :keyword,
                                ''
                            )
                        )
                    ) / :keywordLength AS term_frequency
                FROM posts p
                CROSS JOIN recent_boundary rb
                WHERE p.post_id >= rb.min_post_id
                  AND (
                      p.title
                          LIKE :likePattern ESCAPE '!'
                      OR p.content
                          LIKE :likePattern ESCAPE '!'
                  )
            ),
            author_candidates AS (
                SELECT
                    p.post_id,
                    1 AS field_priority,
                    (
                        CHAR_LENGTH(u.nickname)
                        - CHAR_LENGTH(
                            REPLACE(
                                u.nickname,
                                :keyword,
                                ''
                            )
                        )
                    ) / :keywordLength AS term_frequency
                FROM users u
                JOIN posts p
                  ON p.user_id = u.user_id
                CROSS JOIN recent_boundary rb
                WHERE MATCH(u.nickname)
                      AGAINST(
                          :fullTextKeyword IN BOOLEAN MODE
                      )
                  AND u.nickname
                      LIKE :likePattern ESCAPE '!'
                  AND p.post_id >= rb.min_post_id
            ),
            candidate_rows AS (
                SELECT
                    post_id,
                    field_priority,
                    term_frequency
                FROM post_candidates

                UNION ALL

                SELECT
                    post_id,
                    field_priority,
                    term_frequency
                FROM author_candidates
            ),
            deduplicated_candidates AS (
                SELECT
                    post_id,
                    field_priority,
                    term_frequency,
                    ROW_NUMBER() OVER (
                        PARTITION BY post_id
                        ORDER BY
                            field_priority DESC,
                            term_frequency DESC
                    ) AS duplicate_order
                FROM candidate_rows
            ),
            selected_candidates AS (
                SELECT
                    post_id,
                    field_priority,
                    term_frequency
                FROM deduplicated_candidates
                WHERE duplicate_order = 1
                ORDER BY
                    field_priority DESC,
                    term_frequency DESC,
                    post_id DESC
                LIMIT :limit
                OFFSET :offset
            )
            SELECT
                p.post_id,
                p.title,
                SUBSTRING(
                    p.content,
                    1,
                    :contentLength
                ) AS content_preview,
                p.image_url,
                p.created_at,
                p.like_count,
                p.comment_count,
                p.view_count,
                u.user_id,
                u.nickname,
                u.profile_image_url
            FROM selected_candidates c
            JOIN posts p
              ON p.post_id = c.post_id
            JOIN users u
              ON u.user_id = p.user_id
            ORDER BY
                c.field_priority DESC,
                c.term_frequency DESC,
                c.post_id DESC
            """;

        MapSqlParameterSource parameters =
                commonParameters(keyword, limit)
                        .addValue(
                                "keyword",
                                keyword
                        )
                        .addValue(
                                "keywordLength",
                                keyword.codePointCount(
                                        0,
                                        keyword.length()
                                )
                        )
                        .addValue(
                                "fullTextKeyword",
                                toBooleanPhrase(keyword)
                        )
                        .addValue(
                                "boundaryOffset",
                                RECENT_SCAN_WINDOW - 1
                        )
                        .addValue(
                                "offset",
                                offset
                        );

        return jdbcTemplate.query(
                sql,
                parameters,
                postRowMapper()
        );
    }

    public List<PostListItemResponseDTO> searchRelevanceByFullText(
            String keyword,
            int offset,
            int limit
    ) {
        String sql = """
            WITH post_candidates AS (
                SELECT
                    p.post_id,
                    CASE
                        WHEN p.title
                             LIKE :likePattern ESCAPE '!'
                            THEN 3
                        ELSE 2
                    END AS field_priority,
                    MATCH(p.title, p.content)
                        AGAINST(
                            :fullTextKeyword IN BOOLEAN MODE
                        ) AS relevance_score
                FROM posts p
                WHERE MATCH(p.title, p.content)
                      AGAINST(
                          :fullTextKeyword IN BOOLEAN MODE
                      )
                  AND (
                      p.title
                          LIKE :likePattern ESCAPE '!'
                      OR p.content
                          LIKE :likePattern ESCAPE '!'
                  )
            ),
            author_candidates AS (
                SELECT
                    p.post_id,
                    1 AS field_priority,
                    MATCH(u.nickname)
                        AGAINST(
                            :fullTextKeyword IN BOOLEAN MODE
                        ) AS relevance_score
                FROM users u
                JOIN posts p
                  ON p.user_id = u.user_id
                WHERE MATCH(u.nickname)
                      AGAINST(
                          :fullTextKeyword IN BOOLEAN MODE
                      )
                  AND u.nickname
                      LIKE :likePattern ESCAPE '!'
            ),
            candidate_rows AS (
                SELECT
                    post_id,
                    field_priority,
                    relevance_score
                FROM post_candidates

                UNION ALL

                SELECT
                    post_id,
                    field_priority,
                    relevance_score
                FROM author_candidates
            ),
            deduplicated_candidates AS (
                SELECT
                    post_id,
                    field_priority,
                    relevance_score,
                    ROW_NUMBER() OVER (
                        PARTITION BY post_id
                        ORDER BY
                            field_priority DESC,
                            relevance_score DESC
                    ) AS duplicate_order
                FROM candidate_rows
            ),
            selected_candidates AS (
                SELECT
                    post_id,
                    field_priority,
                    relevance_score
                FROM deduplicated_candidates
                WHERE duplicate_order = 1
                ORDER BY
                    field_priority DESC,
                    relevance_score DESC,
                    post_id DESC
                LIMIT :limit
                OFFSET :offset
            )
            SELECT
                p.post_id,
                p.title,
                SUBSTRING(
                    p.content,
                    1,
                    :contentLength
                ) AS content_preview,
                p.image_url,
                p.created_at,
                p.like_count,
                p.comment_count,
                p.view_count,
                u.user_id,
                u.nickname,
                u.profile_image_url
            FROM selected_candidates c
            JOIN posts p
              ON p.post_id = c.post_id
            JOIN users u
              ON u.user_id = p.user_id
            ORDER BY
                c.field_priority DESC,
                c.relevance_score DESC,
                c.post_id DESC
            """;

        MapSqlParameterSource parameters =
                commonParameters(keyword, limit)
                        .addValue(
                                "fullTextKeyword",
                                toBooleanPhrase(keyword)
                        )
                        .addValue(
                                "offset",
                                offset
                        );

        return jdbcTemplate.query(
                sql,
                parameters,
                postRowMapper()
        );
    }

    private MapSqlParameterSource commonParameters(
            String keyword,
            int limit
    ) {
        return new MapSqlParameterSource()
                .addValue(
                        "likePattern",
                        toLikePattern(keyword)
                )
                .addValue(
                        "contentLength",
                        LIST_CONTENT_MAX_LENGTH
                )
                .addValue(
                        "limit",
                        limit
                );
    }

    private String toLikePattern(String keyword) {
        String escapedKeyword = keyword
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");

        return "%" + escapedKeyword + "%";
    }

    private String toBooleanPhrase(String keyword) {
        return "\"" + keyword + "\"";
    }

    private RowMapper<PostListItemResponseDTO> postRowMapper() {
        return (resultSet, rowNumber) -> {
            PostAuthorResponseDTO author =
                    new PostAuthorResponseDTO(
                            resultSet.getInt("user_id"),
                            resultSet.getString("nickname"),
                            resultSet.getString(
                                    "profile_image_url"
                            )
                    );

            Timestamp createdAt =
                    resultSet.getTimestamp("created_at");

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
