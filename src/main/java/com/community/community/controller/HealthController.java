package com.community.community.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    // liveness 전용
    // 프로세스 생존만 확인
    @GetMapping("/health/live")
    public ResponseEntity<Map<String, String>> live() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    // readiness 전용
    // 트래픽을 받을 수 있는 상태인지 확인
    // 커넥션 풀 고갈로 인해 요청을 받지 못 할 수도 있으니, DB 커넥션 확보 확인
    @GetMapping("/health/ready")
    public ResponseEntity<Map<String, String>> ready() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(1)) {
                return ResponseEntity.ok(Map.of("status", "UP"));
            }
            log.warn("readiness check failed: connection is not valid");
        } catch (SQLException e) {
            log.warn("readiness check failed: {}", e.getMessage());
        }
        return ResponseEntity.status(503).body(Map.of("status", "DOWN"));
    }
}