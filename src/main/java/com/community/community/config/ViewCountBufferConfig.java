package com.community.community.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties(ViewCountBufferProperties.class)
public class ViewCountBufferConfig {

    @Configuration
    @EnableScheduling
    @ConditionalOnProperty(
            name = "view-count.buffer.enabled",
            havingValue = "true"
    )
    static class SchedulingConfig {
    }
}
