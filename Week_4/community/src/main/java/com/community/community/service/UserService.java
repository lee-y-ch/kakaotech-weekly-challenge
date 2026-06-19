package com.community.community.service;

import com.community.community.dto.*;
import com.community.community.entity.User;
import com.community.community.exception.BusinessException;
import com.community.community.exception.ErrorCode;
import com.community.community.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입 처리 로직
    public void register(RegisterRequestDTO request) {
        String email = request.getEmail();
        String password = request.getPassword();
        String nickname = request.getNickname();
        String profileImage = request.getProfileImageUrl();

        // 각 에러 처리에 대한 응답 코드 매핑은 UserController에서 진행

        // email 중복 검증 (409)
        if (userRepository.existsByEmail(email)) {
            // IllegalStateException: 형식은 올바르나 상태가 올바르지 않은 상황에 대한 에러 처리
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // nickname 중복 검증 (409)
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 저장 로직
        User user = new User(email, encodedPassword, nickname, profileImage);
        userRepository.save(user);
    }

    // User를 찾는 메서드
    public User getUser(int pathUserId, int currentUserId) {

        // URL 경로의 userId와 JWT에서 추출한 현재 userId가 일치하는지 확인한다. (403)
        if (pathUserId != currentUserId) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // findById()는 조회 결과가 없을 수 있기 때문에 Optional<User>를 반환한다.
        // 값이 있으면 User를 그대로 반환하고,
        // 값이 없으면 orElseThrow() 안의 예외를 생성해서 user_not_found로 처리한다.
        return userRepository.findById(pathUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    // @Transactional로 설정해서, userRepository.save(user)를 다시 호출하지 않아도 됨.
    // user가 영속 상태이기 때문에 트랜잭션이 끝날 때 JPA가 변경된 값을 감지하고 UPDATE를 실행
    @Transactional
    public GetUserResponseDTO updateUser(int pathUserId, int currentUserId, UserUpdateRequestDTO request) {
        if (pathUserId != currentUserId) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        User user = userRepository.findById(pathUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (userRepository.existsByNicknameAndUserIdNot(request.getNickname(), pathUserId)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        user.updateProfile(request.getNickname(), request.getProfileImageUrl());

        UserResponseDTO userResponse = new UserResponseDTO(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl()
        );

        return new GetUserResponseDTO(userResponse);
    }

    @Transactional
    public PasswordUpdateResponseDTO updatePassword(
            int pathUserId,
            int currentUserId,
            PasswordUpdateRequestDTO request
    ) {
        if (pathUserId != currentUserId) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        User user = userRepository.findById(pathUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new BusinessException(ErrorCode.INVALID_UPDATE_PASSWORD_REQUEST);
        }

        user.updatePassword(passwordEncoder.encode(request.getPassword()));

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
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        User user = userRepository.findById(pathUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public void checkNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_NICKNAME_CHECK_REQUEST);
        }

        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    @Transactional(readOnly = true)
    public void checkEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_CHECK_REQUEST);
        }

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

}
