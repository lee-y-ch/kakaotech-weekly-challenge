package com.community.community.controller;

import com.community.community.ApiResponse;
import com.community.community.dto.LoginRequestDTO;
import com.community.community.dto.LoginResponseDTO;
import com.community.community.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try {
            String sessionId = authService.login(request);

            LoginResponseDTO data = new LoginResponseDTO();
            ApiResponse<LoginResponseDTO> response = new ApiResponse<>("login_success", data);

            data.setSessionId(sessionId);

            return ResponseEntity.ok(response);

            // 필수값 누락 등 잘못된 로그인 요청에 대한 400 응답
        } catch (IllegalArgumentException e) {
            ApiResponse<LoginResponseDTO> response = new ApiResponse<>(e.getMessage(), null);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

            // 이메일 또는 비밀번호 불일치에 대한 401 응답
        } catch (SecurityException e) {
            ApiResponse<LoginResponseDTO> response = new ApiResponse<>(e.getMessage(), null);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

            // 예상하지 못한 서버 내부 오류에 대한 500 응답
        } catch (Exception e) {
            ApiResponse<LoginResponseDTO> response = new ApiResponse<>("internal_server_error", null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/auth")
    public ResponseEntity<?> logout(
            // Header 누락도 인증 실패로 처리하기 위해 required = false로 설정한다.
            // 기본값(true)을 사용하면 Controller 진입 전에 Spring이 400을 반환할 수 있다.
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        try {
            authService.logout(authorization);

            return ResponseEntity.noContent().build();

            // session_id가 없거나 만료되어 인증에 실패한 경우: 401
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("unauthorized", null)
            );

            // 서버 내부 오류: 500
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("internal_server_error", null)
            );
        }
    }

}
