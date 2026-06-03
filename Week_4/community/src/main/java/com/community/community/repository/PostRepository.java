package com.community.community.repository;

import com.community.community.dto.PostAuthorResponseDTO;
import com.community.community.dto.PostListItemResponseDTO;
import com.community.community.entity.Post;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PostRepository {

    private final JdbcTemplate jdbcTemplate;

    public PostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int save(String title, String content, String image_url, int userId) {
        String sql = """
                INSERT INTO posts (title, content, image_url, user_id)
                VALUES (?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql, title, content, image_url, userId);

        // post 생성 api 응답에서 반환할 때 활용할 post_id를 return한다.
        // LAST_INSERT_ID()로 가장 최근에 INSERT된 row를 찾는다.
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
    }

    // 저장한 post를 응답 dto에 담아서 반환하기 위해 postId로 찾은 post 객체를 리턴
    public Post findById(int postId) {
        String sql = """
            SELECT post_id, user_id, title, content, image_url, created_at,
                   view_count, like_count, comment_count
            FROM posts
            WHERE post_id = ?
            """;

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Post post = new Post();
                post.setPostId(rs.getInt("post_id"));
                post.setUserId(rs.getInt("user_id"));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setImageUrl(rs.getString("image_url"));
                post.setCreatedAt(rs.getString("created_at"));
                post.setViewCount(rs.getInt("view_count"));
                post.setLikeCount(rs.getInt("like_count"));
                post.setCommentCount(rs.getInt("comment_count"));
                return post;
            }, postId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


    // cursor가 0이면 첫 목록 조회이므로 조건 없이 최신 게시글부터 조회한다.
    // cursor가 0이 아니면 마지막으로 조회한 post_id보다 작은 게시글만 조회한다.
    // limit은 size + 1로 전달해서 다음 페이지가 있는지 확인하는 데 사용한다.
    public List<PostListItemResponseDTO> findPostsByCursor(int cursor, int limit) {
        String sql = """
            SELECT p.post_id,
                   p.title,
                   p.created_at,
                   p.like_count,
                   p.comment_count,
                   p.view_count,
                   u.user_id,
                   u.nickname,
                   u.profile_image
            FROM posts p
            JOIN users u ON p.user_id = u.user_id
            WHERE (? = 0 OR p.post_id < ?)
            ORDER BY p.post_id DESC
            LIMIT ?
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            PostAuthorResponseDTO author = new PostAuthorResponseDTO();

            author.setUserId(rs.getInt("user_id"));
            author.setNickname(rs.getString("nickname"));
            author.setProfileImage(rs.getString("profile_image"));

            PostListItemResponseDTO post = new PostListItemResponseDTO();

            post.setPostId(rs.getInt("post_id"));
            post.setTitle(rs.getString("title"));
            post.setAuthor(author);
            post.setCreatedAt(rs.getString("created_at"));
            post.setLikeCount(rs.getInt("like_count"));
            post.setCommentCount(rs.getInt("comment_count"));
            post.setViewCount(rs.getInt("view_count"));

            return post;

        }, cursor, cursor, limit);
    }

    public Integer findAuthorIdByPostId(int postId) {
        String sql = """
            SELECT user_id
            FROM posts
            WHERE post_id = ?
            """;

        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, postId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void update(int postId, String title, String content, String imageUrl) {
        String sql = """
            UPDATE posts
            SET title = ?,
                content = ?,
                image_url = ?
            WHERE post_id = ?
            """;

        jdbcTemplate.update(sql, title, content, imageUrl, postId);
    }

    public void deleteById(int postId) {
        String sql = """
            DELETE FROM posts
            WHERE post_id = ?
            """;

        jdbcTemplate.update(sql, postId);
    }

    public void increaseCommentCount(int postId) {
        String sql = """
            UPDATE posts
            SET comment_count = comment_count + 1
            WHERE post_id = ?
            """;

        jdbcTemplate.update(sql, postId);
    }

    public void decreaseCommentCount(int postId) {
        // GREATEST()를 사용하여 댓글이 음수다 되는 경우를 방지
        String sql = """
            UPDATE posts
            SET comment_count = GREATEST(comment_count - 1, 0)
            WHERE post_id = ?
            """;

        jdbcTemplate.update(sql, postId);
    }

    public void increaseLikeCount(int postId) {
        String sql = """
            UPDATE posts
            SET like_count = like_count + 1
            WHERE post_id = ?
            """;

        jdbcTemplate.update(sql, postId);
    }

    public void decreaseLikeCount(int postId) {
        String sql = """
            UPDATE posts
            SET like_count = GREATEST(like_count - 1, 0)
            WHERE post_id = ?
            """;

        jdbcTemplate.update(sql, postId);
    }

    public int findLikeCountByPostId(int postId) {
        String sql = """
            SELECT like_count
            FROM posts
            WHERE post_id = ?
            """;

        Integer likeCount = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                postId
        );

        return likeCount;
    }

}
