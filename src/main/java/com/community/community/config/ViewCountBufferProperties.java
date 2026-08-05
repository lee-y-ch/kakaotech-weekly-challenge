package com.community.community.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "view-count.buffer")
public record ViewCountBufferProperties(
        boolean enabled,
        int partitions,
        long flushIntervalMs,
        Duration lockTtl,
        Duration totalTtl
) {
    public ViewCountBufferProperties {
        if (partitions <= 0) {
            throw new IllegalArgumentException("view-count.buffer.partitions must be positive");
        }
        if (flushIntervalMs <= 0) {
            throw new IllegalArgumentException("view-count.buffer.flush-interval-ms must be positive");
        }
    }
}
