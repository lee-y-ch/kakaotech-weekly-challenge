package com.community.community.service;

import com.community.community.auth.JwtProvider;
import com.community.community.dto.LoginRequestDTO;
import com.community.community.dto.LoginResultDTO;
import com.community.community.entity.User;
import com.community.community.exception.BusinessException;
import com.community.community.exception.ErrorCode;
import com.community.community.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * AuthService 단위 테스트
 * Repository/JwtProvider/PasswordEncoder 를 Mock 으로 대체해 DB 없이 로그인 로직만 검증
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock JwtProvider jwtProvider;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks AuthService authService;

    private LoginRequestDTO loginRequest(String email, String password) {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    private User user() {
        return new User("user@example.com", "encoded-pw", "nick", "http://img/profile.png");
    }

    @Test
    @DisplayName("올바른 이메일/비밀번호로 로그인하면 액세스 토큰을 발급한다")
    void login_success_issuesToken() {
        // given
        User user = user();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw-pw", "encoded-pw")).thenReturn(true);
        when(jwtProvider.createAccessToken(user.getUserId(), user.getEmail(), user.getNickname()))
                .thenReturn("issued-token");

        // when
        LoginResultDTO result = authService.login(loginRequest("user@example.com", "raw-pw"));

        // then
        assertThat(result.getAccessToken()).isEqualTo("issued-token");
    }

    @Test
    @DisplayName("존재하지 않는 이메일이면 INVALID_EMAIL_OR_PASSWORD 예외가 발생한다")
    void login_fail_whenEmailNotFound() {
        // given
        when(userRepository.findByEmail("none@example.com")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest("none@example.com", "raw-pw")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_EMAIL_OR_PASSWORD);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 INVALID_EMAIL_OR_PASSWORD 예외가 발생한다")
    void login_fail_whenPasswordMismatch() {
        // given
        User user = user();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-pw", "encoded-pw")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest("user@example.com", "wrong-pw")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_EMAIL_OR_PASSWORD);
    }

    @Test
    @DisplayName("빈 토큰으로 현재 사용자 조회 시 UNAUTHORIZED 예외가 발생한다")
    void getCurrentUserId_fail_whenBlankToken() {
        assertThatThrownBy(() -> authService.getCurrentUserId(""))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }
}
