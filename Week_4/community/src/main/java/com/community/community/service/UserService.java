package com.community.community.service;

import com.community.community.dto.*;
import com.community.community.entity.User;
import com.community.community.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 회원가입 처리 로직
    public void register(RegisterRequestDTO request) {
        String email = request.getEmail();
        String password = request.getPassword();
        String nickname = request.getNickname();
        String profileImage = request.getProfileImage();

        // 각 에러 처리에 대한 응답 코드 매핑은 UserController에서 진행

        // 필수값 검사 (400)
        if (email == null || email.isBlank()
                || password == null || password.isBlank()
                || nickname == null || nickname.isBlank()
                || profileImage == null || profileImage.isBlank()) {
            // IllegalArgumentException: 잘못된 인자에 대한 에러 처리
            throw new IllegalArgumentException("invalid_register_request");
        }

        // email 중복 검증 (409)
        if (userRepository.existsByEmail(email)) {
            // IllegalStateException: 형식은 올바르나 상태가 올바르지 않은 상황에 대한 에러 처리
            throw new IllegalStateException("email_already_exists");
        }

        // nickname 중복 검증 (409)
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalStateException("nickname_already_exists");
        }

        // 저장 로직
        userRepository.save(email, password, nickname, profileImage);
    }

    // User를 찾는 메서드
    public User getUser(int pathUserId, int currentUserId) {

        // url 경로에 있는 userId와 sessionId로 찾은 userId가 일치하는지 확인 (403 에러)
        if (pathUserId != currentUserId) {
            throw new SecurityException("forbidden");
        }

        User user = userRepository.findById(pathUserId);

        if (user == null) {
            throw new IllegalStateException("user_not_found");
        }

        return user;
    }

    public GetUserResponseDTO updateUser(int pathUserId, int currentUserId, UserUpdateRequestDTO request) {
        if (pathUserId != currentUserId) {
            throw new SecurityException("forbidden");
        }

        User user = userRepository.findById(pathUserId);

        if (user == null) {
            throw new IllegalStateException("user_not_found");
        }

        if (request.getNickname() == null || request.getNickname().isBlank()) {
            throw new IllegalArgumentException("invalid_update_user_request");
        }

        if (request.getNickname().length() > 10) {
            throw new IllegalArgumentException("invalid_update_user_request");
        }

        if (userRepository.existsByNicknameExceptUserId(request.getNickname(), pathUserId)) {
            throw new IllegalStateException("nickname_already_exists");
        }

        userRepository.updateUser(
                pathUserId,
                request.getNickname(),
                request.getProfileImage()
        );

        User updatedUser = userRepository.findById(pathUserId);

        UserResponseDTO userResponse = new UserResponseDTO();

        userResponse.setUserId(updatedUser.getUserId());
        userResponse.setEmail(updatedUser.getEmail());
        userResponse.setNickname(updatedUser.getNickname());
        userResponse.setProfileImage(updatedUser.getProfileImage());

        return new GetUserResponseDTO(userResponse);
    }

    public PasswordUpdateResponseDTO updatePassword(
            int pathUserId,
            int currentUserId,
            PasswordUpdateRequestDTO request
    ) {
        if (pathUserId != currentUserId) {
            throw new SecurityException("forbidden");
        }

        User user = userRepository.findById(pathUserId);

        if (user == null) {
            throw new IllegalStateException("user_not_found");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()
                || request.getPasswordConfirm() == null || request.getPasswordConfirm().isBlank()) {
            throw new IllegalArgumentException("invalid_update_password_request");
        }

        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("invalid_update_password_request");
        }

        if (!isValidPassword(request.getPassword())) {
            throw new IllegalArgumentException("invalid_update_password_request");
        }

        userRepository.updatePassword(pathUserId, request.getPassword());

        return new PasswordUpdateResponseDTO(pathUserId);
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8 || password.length() > 20) {
            return false;
        }

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);

            if (Character.isUpperCase(ch)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(ch)) {
                hasLowerCase = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            } else {
                hasSpecialChar = true;
            }
        }

        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
    }

    public void deleteUser(int pathUserId, int currentUserId) {
        if (pathUserId != currentUserId) {
            throw new SecurityException("forbidden");
        }

        User user = userRepository.findById(pathUserId);

        if (user == null) {
            throw new IllegalStateException("user_not_found");
        }

        userRepository.deleteById(pathUserId);
    }

}
