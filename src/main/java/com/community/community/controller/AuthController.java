package com.community.community.controller;

import com.community.community.ApiResponse;
import com.community.community.auth.CurrentUserId;
import com.community.community.dto.LoginRequestDTO;
import com.community.community.dto.LoginResultDTO;
import com.community.community.dto.UserResponseDTO;
import com.community.community.entity.User;
import com.community.community.service.AuthService;
import com.community.community.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {

        this.authService = authService;
        this.userService= userService;
    }

    @PostMapping("/auth")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResultDTO result = authService.login(request);

        // accessToken은 JS에서 직접 다루지 않도록 HttpOnly Cookie로 내려준다.
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", result.getAccessToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7200)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .body(new ApiResponse<>("login_success", result.getResponse()));
    }

    @DeleteMapping("/auth")
    public ResponseEntity<?> logout(
            @CurrentUserId int currentUserId
    ) {
        // 서버에 저장된 세션이 없으므로, 로그아웃은 브라우저의 accessToken 쿠키를 만료시키는 방식으로 처리한다.
        ResponseCookie expiredCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .build();

    }

    // 프론트 코드의 /auth/check api 호출에 답하기 위해 생성
    @GetMapping("/auth/check")
    public ResponseEntity<?> checkAuth(
            @CurrentUserId int currentUserId
    ) {
        // /auth/check는 URL에 조회 대상 userId가 없다.
        // 현재 로그인 사용자를 확인하는 API이므로 조회 대상과 인증 사용자를 같은 값으로 넘긴다.
        User user = userService.getUser(currentUserId, currentUserId);

        UserResponseDTO data = new UserResponseDTO(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl()
        );

        return ResponseEntity.ok(
                new ApiResponse<>("auth_check_success", data)
        );
    }
}
