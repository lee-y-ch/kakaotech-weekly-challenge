package com.community.community.controller;

import com.community.community.ApiResponse;
import com.community.community.auth.CurrentUserId;
import com.community.community.dto.*;
import com.community.community.entity.User;
import com.community.community.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ResponseEntity: spring에서 HTTP 응답을 만들어 반환할 수 있게 해주는 객체
    @PostMapping("/users")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO request) {

        // UserService 호출해서 서비스 로직에서 요청 body 데이터 활용할 수 있도록 request 넘기기
        userService.register(request);

        // 응답 - Map을 사용해서 응답을 만드려고 했는데 null이 허용이 안 돼서, HashMap을 사용하려 했는데 동일한 응답 형태가
        //       많은 api에서 반복되기 때문에 ApiResponse라는 공통된 응답 형식을 처리하는 클래스를 별도로 생성
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<Void>("register_success", null)
        );
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUser(
        @PathVariable int userId,
        @CurrentUserId int currentUserId
    ) {
        User user = userService.getUser(userId, currentUserId);

        // api 명세에 있는 응답 (password를 제외한)을 맞추기 위해 password가 없는 응답 형태를 DTO로 만들어 반환
        UserResponseDTO userResponseDTO = new UserResponseDTO(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl()
        );

        GetUserResponseDTO data = new GetUserResponseDTO(userResponseDTO);

        return ResponseEntity.ok(
                new ApiResponse<>("get_user_success", data)
        );
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable int userId,
            @CurrentUserId int currentUserId,
            @Valid @RequestBody UserUpdateRequestDTO request
    ) {
        GetUserResponseDTO data = userService.updateUser(userId, currentUserId, request);

        return ResponseEntity.ok(
                new ApiResponse<>("update_user_success", data)
        );
    }

    @PatchMapping("/users/{userId}/password")
    public ResponseEntity<ApiResponse<PasswordUpdateResponseDTO>> updatePassword(
            @PathVariable int userId,
            @CurrentUserId int currentUserId,
            @Valid @RequestBody PasswordUpdateRequestDTO request
    ) {
        PasswordUpdateResponseDTO data = userService.updatePassword(
                userId,
                currentUserId,
                request
        );

        return ResponseEntity.ok(
                new ApiResponse<>("update_password_success", data)
        );
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable int userId,
            @CurrentUserId int currentUserId
    ) {
        userService.deleteUser(userId, currentUserId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/nickname/check")
    public ResponseEntity<?> checkNickname(@RequestParam String nickname) {

        userService.checkNickname(nickname);

        return ResponseEntity.ok(
                new ApiResponse<>("nickname_available", null)
        );
    }

    @GetMapping("/users/email/check")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {

        userService.checkEmail(email);

        return ResponseEntity.ok(
                new ApiResponse<>("email_available", null)
        );
    }
}
