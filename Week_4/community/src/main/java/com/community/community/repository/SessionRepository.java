package com.community.community.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class SessionRepository {

    private final JdbcTemplate jdbcTemplate;

    public SessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // sessions 테이블에 값을 저장하는 메서드
    // created_at은 CURRENT_TIMESTAMP여서 저장하지 않아도 됨.
    public void save(String sessionId, int userId, LocalDateTime expiredAt) {
        String sql = """
                INSERT INTO sessions (session_id, user_id, expired_at)
                VALUES (?, ?, ?)
                """;

        jdbcTemplate.update(sql, sessionId, userId, expiredAt);
    }

    // SessionId를 통해 User를 찾는 메서드
    // 반환값이 null이 되는 경우를 대응하기 위해 int가 아닌 Integer
    public Integer findUserIdBySessionId(String sessionId) {
        // 만료 시각을 조회 조건에 포함하여 존재하지만 만료된 세션도 인증 실패로 처리한다.
        String sql = """
            SELECT user_id
            FROM sessions
            WHERE session_id = ?
              AND expired_at > NOW()
            """;

        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, sessionId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void deleteBySessionId(String sessionId) {
        String sql = """
            DELETE FROM sessions
            WHERE session_id = ?
            """;

        jdbcTemplate.update(sql, sessionId);
    }

}
