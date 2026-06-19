package com.community.community.service;

import com.community.community.auth.JwtProvider;
import com.community.community.dto.LoginRequestDTO;
import com.community.community.dto.LoginResponseDTO;
import com.community.community.dto.LoginResultDTO;
import com.community.community.dto.UserResponseDTO;
import com.community.community.entity.User;
import com.community.community.exception.BusinessException;
import com.community.community.exception.ErrorCode;
import com.community.community.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;


    public AuthService(UserRepository userRepository, JwtProvider jwtProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }

    // login 메서드
    @Transactional(readOnly = true)
    public LoginResultDTO login(LoginRequestDTO loginRequestDTO) {
        String email = loginRequestDTO.getEmail();
        String password = loginRequestDTO.getPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_EMAIL_OR_PASSWORD));

        // 틀린 email or password (401)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_OR_PASSWORD);
        }

        // 서버에 세션을 저장하지 않고, 사용자 식별 정보를 담은 JWT를 발급한다.
        String accessToken = jwtProvider.createAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getNickname()
        );

        UserResponseDTO userResponse = new UserResponseDTO(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl()
        );

        LoginResponseDTO loginResponse = new LoginResponseDTO(userResponse);

        return new LoginResultDTO(loginResponse, accessToken);
    }

    // 현재 사용자를 확인하는 메서드
    // HttpOnly Cookie로 전달된 JWT를 검증하고, subject에 저장된 userId를 현재 사용자로 사용한다.
    public int getCurrentUserId(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (!jwtProvider.validateAccessToken(accessToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return jwtProvider.getUserId(accessToken);
    }

    public void logout(String accessToken) {
        // 현재는 Access Token만 사용하므로 서버에서 삭제할 세션 데이터가 없다.
        // 따라서 로그아웃 요청에서는 토큰이 유효한지만 확인하고,
        // 실제 로그아웃 처리는 Controller에서 accessToken 쿠키를 만료시키는 방식으로 처리한다.
        getCurrentUserId(accessToken);
    }

}
