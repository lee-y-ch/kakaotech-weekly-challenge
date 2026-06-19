package com.community.community.dto;

import lombok.Getter;

// API 응답의 data 안에 들어갈 user 객체를 표현하는 필드
// 실제 사용자 정보는 UserResponseDTO가 담당하고,
// 이 클래스는 { "user": { ... } } 형태로 한 번 감싸는 역할
@Getter
public class GetUserResponseDTO {

    private final UserResponseDTO user;

    public GetUserResponseDTO(UserResponseDTO user) {
        this.user = user;
    }
}