package com.community.community.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "users")
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 10)
    private String nickname;

    @Column(name = "profile_image_url", nullable = false, length = 500)
    private String profileImageUrl;

    protected User() {
    }

    public User(String email, String password, String nickname, String profileImageUrl) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    // setter를 모두 설정하지 않고 필요한 속성에 대해서만 따로 만듦
    public void updateProfile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}
