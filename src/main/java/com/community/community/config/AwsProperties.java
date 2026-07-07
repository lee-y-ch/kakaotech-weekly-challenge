package com.community.community.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

// @Value 여러 개가 아닌 하나의 객체로 관리해보기 (관련 설정을 하나의 객체에 묶어서 활용할 수 있다.)
// 설정값은 바뀔 일이 거의 없기 때문에 record 활용
@Validated
@ConfigurationProperties(prefix = "aws")
public record AwsProperties(
        @NotBlank String region,
        @Valid @NotNull S3Properties s3,
        @Valid @NotNull ImageProperties image,
        @Valid @NotNull PresignedUrlProperties presignedUrl
) {

    public record S3Properties(
            @NotBlank String originalBucket,
            @NotBlank String processedBucket
    ) {
    }

    public record ImageProperties(
            @NotNull URI baseUrl
    ) {
    }

    public record PresignedUrlProperties(
            @Positive long expirationSeconds,
            @NotNull DataSize maxFileSize
    ) {
    }
}