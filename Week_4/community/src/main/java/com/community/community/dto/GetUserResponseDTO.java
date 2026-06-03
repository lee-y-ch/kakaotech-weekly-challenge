package com.community.community.dto;

// API 응답의 data 안에 들어갈 user 객체를 표현하는 필드
// 실제 사용자 정보는 UserResponseDTO가 담당하고,
// 이 클래스는 { "user": { ... } } 형태로 한 번 감싸는 역할
public class GetUserResponseDTO {

    private UserResponseDTO user;

    // 기본 생성자
    public GetUserResponseDTO() {
    }

    // Controller에서 UserResponseDTO를 받아 바로 응답 data 객체를 만들기 위한 생성자
    public GetUserResponseDTO(UserResponseDTO user) {
        this.user = user;
    }

    // 응답 JSON 생성 시 user 필드를 읽기 위한 getter
    public UserResponseDTO getUser() {
        return user;
    }

    // getter를 생성해서, setter도 만들었는데 나중에 UserResponseDTO 값을 바꿀 때 사용할 수 있을 듯
    public void setUser(UserResponseDTO user) {
        this.user = user;
    }
}
