package com.community.community.auth;

import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        // HS256 은 최소 256bit(32byte) 키가 필요하므로 충분한 길이의 시크릿을 Base64 로 인코딩
        String secret = Encoders.BASE64.encode(
                "test-secret-key-for-jwt-provider-unit-test-1234567890".getBytes(StandardCharsets.UTF_8)
        );

        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        properties.setAccessTokenExpSeconds(3600);

        jwtProvider = new JwtProvider(properties);
        jwtProvider.init();   // @PostConstruct 를 수동 호출해 key 를 초기화
    }

    @Test
    @DisplayName("생성한 토큰은 검증을 통과한다")
    void validateAccessToken_returnsTrue_forValidToken() {
        // given
        String token = jwtProvider.createAccessToken(1, "user@example.com", "nick");

        // when
        boolean valid = jwtProvider.validateAccessToken(token);

        // then
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("토큰에서 발급 시 사용한 userId 를 그대로 추출한다")
    void getUserId_returnsOriginalUserId() {
        // given
        int userId = 42;
        String token = jwtProvider.createAccessToken(userId, "user@example.com", "nick");

        // when
        Integer extracted = jwtProvider.getUserId(token);

        // then
        assertThat(extracted).isEqualTo(userId);
    }

    @Test
    @DisplayName("변조된 토큰은 검증에 실패한다")
    void validateAccessToken_returnsFalse_forTamperedToken() {
        // given
        String token = jwtProvider.createAccessToken(1, "user@example.com", "nick");
        String tampered = token + "tamper";

        // when
        boolean valid = jwtProvider.validateAccessToken(tampered);

        // then
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("형식이 아예 잘못된 문자열은 검증에 실패한다")
    void validateAccessToken_returnsFalse_forGarbageToken() {
        // when
        boolean valid = jwtProvider.validateAccessToken("not-a-jwt");

        // then
        assertThat(valid).isFalse();
    }
}
