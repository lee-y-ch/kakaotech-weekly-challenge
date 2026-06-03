package com.community.community.repository;

import com.community.community.entity.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    // MySQL 실제 DB 접근을 위해 JDBC를 활용했습니다.
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // email 중복 탐색
    public boolean existsByEmail(String email) {
        // ?는 자리 표시자로 SQL Injection을 막기 위해 사용했습니다.
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        /*
            sql
            → 실행할 SQL 문장

            Integer.class
            → SQL 실행 결과를 Integer 타입으로 받아오겠다는 뜻

            email
            → SQL의 ? 자리에 넣을 값
         */
        // 반환값이 null일 수도 있기 때문에 int가 아닌 Integer를 사용했습니다.
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    // nickname 중복 탐색
    public boolean existsByNickname(String nickname) {
        String sql = "SELECT COUNT(*) FROM users WHERE nickname = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nickname);
        return count != null && count > 0;
    }

    // user 저장
    public void save(String email, String password, String nickname, String profileImage) {
        String sql = """
                INSERT INTO users (email, password, nickname, profile_image)
                VALUES (?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql, email, password, nickname, profileImage);
    }

    // queryForObject(): SELECT 실행 후 결과 1개를 반환받을 때 사용
    // update(): INSERT, UPDATE, DELETE 실행 후 변경된 row 수를 반환받을 때 사용

    // email로 user 찾기
    public User findByEmail(String email) {
        // email을 조건으로 User Entity 값들을 가져오는 쿼리문
        String sql = """
            SELECT user_id, email, password, nickname, profile_image
            FROM users
            WHERE email = ?
            """;

        // EmptyResultDataAccessException: queryForObject()가 반환한 값이 0개일 때 발생하는 에러
        // -> null을 반환하는 것이 아니고 스프링에 에러를 발생시키기 때문에 try-catch를 활용하여 이 에러가 발생하면 null을 리턴하도록 유도
        // service에서 user == null로 판단 가능


        /*
            1. sql 실행
            2. ? 자리에 email 넣음
            3. users 테이블에서 조건에 맞는 row 조회
            4. 조회된 row를 RowMapper가 User 객체로 변환
            5. User 객체 반환
         */
        // 이 함수는 SQL과 parameter를 기반으로 테이블을 조회하고,
        // 조회 결과를 RowMapper로 꺼내 Java 객체로 변환해서 반환하는 함수다.
        // 즉, DB에서 SELECT 결과 한 건을 가져올 때 사용한다.

        /*
            SQL
            → 무엇을 조회할지 정함

            parameter
            → SQL의 ?에 들어갈 값

            RowMapper
            → 조회된 row를 Java 객체로 변환

            queryForObject
            → 그 최종 객체 하나를 반환
         */
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setNickname(rs.getString("nickname"));
                user.setProfileImage(rs.getString("profile_image"));
                return user;
            }, email);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }

        /*
            rs
            └── 현재 row
                ├── "user_id"       → 1
                ├── "email"         → "test@startupcode.kr"
                ├── "password"      → "test1234"
                ├── "nickname"      → "startup"
                └── "profile_image" → "https://image.kr/img.jpg"

            rs.getInt("user_id")
            -> 현재 row의 user_id 컬럼 값을 int로 꺼냄
         */
    }

    public User findById(int userId) {

        String sql = """
            SELECT user_id, email, password, nickname, profile_image
            FROM users
            WHERE user_id = ?
            """;

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setNickname(rs.getString("nickname"));
                user.setProfileImage(rs.getString("profile_image"));
                return user;
            }, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public boolean existsByNicknameExceptUserId(String nickname, int userId) {
        String sql = """
            SELECT COUNT(*)
            FROM users
            WHERE nickname = ?
              AND user_id != ?
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nickname, userId);

        return count != null && count > 0;
    }

    public void updateUser(int userId, String nickname, String profileImage) {
        String sql = """
            UPDATE users
            SET nickname = ?,
                profile_image = ?
            WHERE user_id = ?
            """;

        jdbcTemplate.update(sql, nickname, profileImage, userId);
    }

    public void updatePassword(int userId, String password) {
        String sql = """
            UPDATE users
            SET password = ?
            WHERE user_id = ?
            """;

        jdbcTemplate.update(sql, password, userId);
    }

    public void deleteById(int userId) {
        String sql = """
            DELETE FROM users
            WHERE user_id = ?
            """;

        jdbcTemplate.update(sql, userId);
    }


}
