package com.community.community.repository;

import com.community.community.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// User Entity를 대상으로 하고, 기본키 타입은 Integer
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // 디폴트로 생성되지 않는 메서드들을 따로 정의

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    // 조회 결과가 없을 수 있으므로 null 대신 Optional<User>를 사용
    //
    Optional<User> findByEmail(String email);

    boolean existsByNicknameAndUserIdNot(String nickname, Integer userId);
}
