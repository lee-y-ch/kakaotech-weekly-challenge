package com.community.community.service;

import com.community.community.config.AwsProperties;
import com.community.community.dto.PresignedImageUploadRequestDTO;
import com.community.community.dto.PresignedImageUploadResponseDTO;
import com.community.community.dto.SignupProfileImageUploadRequestDTO;
import com.community.community.exception.BusinessException;
import com.community.community.exception.ErrorCode;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ImagePresignedUrlService {

    private static final Map<String, String> EXTENSION_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final S3Presigner s3Presigner;
    private final AwsProperties awsProperties;

    public ImagePresignedUrlService(
            S3Presigner s3Presigner,
            AwsProperties awsProperties
    ) {
        this.s3Presigner = s3Presigner;
        this.awsProperties = awsProperties;
    }

    public PresignedImageUploadResponseDTO createAuthenticatedUploadUrl(
            PresignedImageUploadRequestDTO request
    ) {
        return createUploadUrl(
                request.getType(),
                request.getContentType(),
                request.getFileSize()
        );
    }

    public PresignedImageUploadResponseDTO createSignupProfileUploadUrl(
            SignupProfileImageUploadRequestDTO request
    ) {
        return createUploadUrl(
                "profile",
                request.getContentType(),
                request.getFileSize()
        );
    }

    private PresignedImageUploadResponseDTO createUploadUrl(
            String rawType,
            String rawContentType,
            Long fileSize
    ) {
        String type = normalizeType(rawType);
        String contentType = normalizeContentType(rawContentType);

        validateFileSize(fileSize);

        String extension = EXTENSION_BY_CONTENT_TYPE.get(contentType);
        String identifier = UUID.randomUUID().toString();

        String originalKey = type + "/" + identifier + extension;
        String processedKey = type + "/" + identifier + ".webp";

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsProperties.s3().originalBucket())
                .key(originalKey)
                .contentType(contentType)
                .build();

        long expirationSeconds =
                awsProperties.presignedUrl().expirationSeconds();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(
                                Duration.ofSeconds(expirationSeconds)
                        )
                        .putObjectRequest(putObjectRequest)
                        .build();

        try {
            PresignedPutObjectRequest presignedRequest =
                    s3Presigner.presignPutObject(presignRequest);

            return new PresignedImageUploadResponseDTO(
                    presignedRequest.url().toString(),
                    originalKey,
                    processedKey,
                    buildImageUrl(processedKey),
                    contentType,
                    expirationSeconds
            );
        } catch (SdkClientException e) {
            throw new BusinessException(
                    ErrorCode.PRESIGNED_URL_CREATION_FAILED
            );
        }
    }

    private String normalizeType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_IMAGE_UPLOAD_REQUEST
            );
        }

        String type = rawType.trim().toLowerCase(Locale.ROOT);

        if (!type.equals("post") && !type.equals("profile")) {
            throw new BusinessException(
                    ErrorCode.INVALID_IMAGE_UPLOAD_REQUEST
            );
        }

        return type;
    }

    private String normalizeContentType(String rawContentType) {
        if (rawContentType == null || rawContentType.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_IMAGE_UPLOAD_REQUEST
            );
        }

        String contentType =
                rawContentType.trim().toLowerCase(Locale.ROOT);

        if (!EXTENSION_BY_CONTENT_TYPE.containsKey(contentType)) {
            throw new BusinessException(
                    ErrorCode.UNSUPPORTED_IMAGE_TYPE
            );
        }

        return contentType;
    }

    private void validateFileSize(Long fileSize) {
        if (fileSize == null || fileSize <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_IMAGE_UPLOAD_REQUEST
            );
        }

        long maxFileSize =
                awsProperties.presignedUrl()
                        .maxFileSize()
                        .toBytes();

        if (fileSize > maxFileSize) {
            throw new BusinessException(
                    ErrorCode.IMAGE_FILE_TOO_LARGE
            );
        }
    }

    private String buildImageUrl(String processedKey) {
        String baseUrl = awsProperties.image()
                .baseUrl()
                .toString();

        if (baseUrl.endsWith("/")) {
            return baseUrl + processedKey;
        }

        return baseUrl + "/" + processedKey;
    }
}