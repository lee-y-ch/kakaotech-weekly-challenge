package com.community.community.service;

import com.community.community.dto.RegisterRequestDTO;
import com.community.community.entity.User;
import com.community.community.exception.BusinessException;
import com.community.community.exception.ErrorCode;
import com.community.community.repository.PostRepository;
import com.community.community.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * UserService 단위 테스트
 * 회원가입 중복 검증과 권한 검증(본인 여부)이 핵심
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PostRepository postRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ImageS3Service imageS3Service;

    @InjectMocks UserService userService;

    private RegisterRequestDTO registerRequest() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("new@example.com");
        dto.setPassword("raw-pw");
        dto.setNickname("nick");
        dto.setProfileImageUrl("http://img/profile.png");
        return dto;
    }

    @Test
    @DisplayName("정상 회원가입 시 비밀번호를 암호화해 저장한다")
    void register_success_encodesPasswordAndSaves() {
        // given
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByNickname("nick")).thenReturn(false);
        when(passwordEncoder.encode("raw-pw")).thenReturn("encoded-pw");

        // when
        userService.register(registerRequest());

        // then: 저장되는 User 의 비밀번호가 암호화된 값인지 확인
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded-pw");
        assertThat(captor.getValue().getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("이미 존재하는 이메일이면 EMAIL_ALREADY_EXISTS 예외가 발생한다")
    void register_fail_whenEmailExists() {
        // given
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.register(registerRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 존재하는 닉네임이면 NICKNAME_ALREADY_EXISTS 예외가 발생한다")
    void register_fail_whenNicknameExists() {
        // given
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByNickname("nick")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.register(registerRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.NICKNAME_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("본인이 아닌 userId 를 조회하면 FORBIDDEN 예외가 발생한다 (권한 검증)")
    void getUser_fail_whenNotOwner() {
        // when & then: pathUserId(1) != currentUserId(2)
        assertThatThrownBy(() -> userService.getUser(1, 2))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("본인이지만 존재하지 않는 유저면 USER_NOT_FOUND 예외가 발생한다")
    void getUser_fail_whenUserNotFound() {
        // given
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUser(1, 1))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}
