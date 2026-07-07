package com.community.community.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(AwsProperties.class)
public class S3Config {

    @Bean
    public S3Client s3Client(AwsProperties awsProperties) {
        Region region = Region.of(awsProperties.region());

        return S3Client.builder()
                .region(region)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(AwsProperties awsProperties) {
        Region region = Region.of(awsProperties.region());

        return S3Presigner.builder()
                .region(region)
                .build();
    }
}