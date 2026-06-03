package com.community.community.service;

import com.community.community.dto.LoginRequestDTO;
import com.community.community.entity.User;
import com.community.community.repository.SessionRepository;
import com.community.community.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    public AuthService(UserRepository userRepository, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    // login 메서드
    public String login(LoginRequestDTO loginRequestDTO) {
        String email = loginRequestDTO.getEmail();
        String password = loginRequestDTO.getPassword();

        // 올바르지 않은 로그인 요청 (400)
        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalArgumentException("invalid_login_request");
        }

        User user = userRepository.findByEmail(email);

        // 틀린 email or password (401)
        if (user == null || !user.getPassword().equals(password)) {
            throw new SecurityException("invalid_email_or_password");
        }

        // UUID를 활용해 고유한 값 생성
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime expiredAt = LocalDateTime.now().plusHours(2);

        sessionRepository.save(sessionId, user.getUserId(), expiredAt);

        return sessionId;
    }

    // 현재 사용자를 확인하는 메서드
    public int getCurrentUserId(String authorization) {
        // 클라이언트가 body로 전달한 user_id는 조작될 수 있으므로 신뢰하지 않는다.
        // 서버가 발급한 session_id를 검증한 뒤 현재 로그인한 사용자를 식별한다.
        if (authorization == null || !authorization.startsWith("Session ")) {
            throw new SecurityException("unauthorized");
        }

        String sessionId = authorization.substring("Session ".length());
        Integer userId = sessionRepository.findUserIdBySessionId(sessionId);

        if (userId == null) {
            throw new SecurityException("unauthorized");
        }

        return userId;
    }

    public void logout(String authorization) {
        if (authorization == null || !authorization.startsWith("Session ")) {
            throw new SecurityException("unauthorized");
        }

        String sessionId = authorization.substring("Session ".length());

        Integer userId = sessionRepository.findUserIdBySessionId(sessionId);

        if (userId == null) {
            throw new SecurityException("unauthorized");
        }

        sessionRepository.deleteBySessionId(sessionId);
    }

}
