package com.community.community.service;

import com.community.community.config.AwsProperties;
import com.community.community.dto.ImageStatusRequestDTO;
import com.community.community.dto.ImageStatusResponseDTO;
import com.community.community.exception.BusinessException;
import com.community.community.exception.ErrorCode;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ImageS3Service {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final Pattern ORIGINAL_KEY_PATTERN = Pattern.compile(
            "^(post|profile)/"
                    + "([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-"
                    + "[0-9a-f]{4}-[0-9a-f]{12})"
                    + "\\.(jpg|png|webp)$"
    );

    private static final Pattern PROCESSED_KEY_PATTERN = Pattern.compile(
            "^(post|profile)/"
                    + "([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-"
                    + "[0-9a-f]{4}-[0-9a-f]{12})"
                    + "\\.webp$"
    );

    private final S3Client s3Client;
    private final AwsProperties awsProperties;

    public ImageS3Service(
            S3Client s3Client,
            AwsProperties awsProperties
    ) {
        this.s3Client = s3Client;
        this.awsProperties = awsProperties;
    }

    public ImageStatusResponseDTO getAuthenticatedImageStatus(
            ImageStatusRequestDTO request
    ) {
        return getImageStatus(request, null);
    }

    public ImageStatusResponseDTO getSignupProfileImageStatus(
            ImageStatusRequestDTO request
    ) {
        return getImageStatus(request, "profile");
    }

    private ImageStatusResponseDTO getImageStatus(
            ImageStatusRequestDTO request,
            String requiredCategory
    ) {
        String originalKey = request.getOriginalKey().trim();
        String processedKey = request.getProcessedKey().trim();

        validateKeyPair(
                originalKey,
                processedKey,
                requiredCategory
        );

        HeadObjectResponse originalObject =
                getRequiredOriginalObject(originalKey);

        validateUploadedObject(
                originalKey,
                processedKey,
                originalObject
        );

        boolean ready = objectExists(
                awsProperties.s3().processedBucket(),
                processedKey
        );

        return new ImageStatusResponseDTO(
                ready,
                processedKey,
                buildImageUrl(processedKey)
        );
    }

    public void deleteImage(
            String originalKey,
            String processedKey
    ) {
        validateKeyPair(originalKey, processedKey, null);

        try {
            deleteObject(
                    awsProperties.s3().originalBucket(),
                    originalKey
            );

            deleteObject(
                    awsProperties.s3().processedBucket(),
                    processedKey
            );
        } catch (SdkException e) {
            throw new BusinessException(
                    ErrorCode.IMAGE_DELETE_FAILED
            );
        }
    }

    private HeadObjectResponse getRequiredOriginalObject(
            String originalKey
    ) {
        try {
            return s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(
                                    awsProperties.s3()
                                            .originalBucket()
                            )
                            .key(originalKey)
                            .build()
            );
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new BusinessException(
                        ErrorCode.IMAGE_NOT_FOUND
                );
            }

            throw new BusinessException(
                    ErrorCode.IMAGE_STATUS_CHECK_FAILED
            );
        } catch (SdkException e) {
            throw new BusinessException(
                    ErrorCode.IMAGE_STATUS_CHECK_FAILED
            );
        }
    }

    private boolean objectExists(
            String bucket,
            String key
    ) {
        try {
            s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );

            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }

            throw new BusinessException(
                    ErrorCode.IMAGE_STATUS_CHECK_FAILED
            );
        } catch (SdkException e) {
            throw new BusinessException(
                    ErrorCode.IMAGE_STATUS_CHECK_FAILED
            );
        }
    }

    private void validateUploadedObject(
            String originalKey,
            String processedKey,
            HeadObjectResponse originalObject
    ) {
        long maxFileSize = awsProperties.presignedUrl()
                .maxFileSize()
                .toBytes();

        if (originalObject.contentLength() > maxFileSize) {
            deleteQuietly(originalKey, processedKey);

            throw new BusinessException(
                    ErrorCode.IMAGE_FILE_TOO_LARGE
            );
        }

        String contentType = originalObject.contentType();

        if (contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(
                contentType.toLowerCase(Locale.ROOT)
        )) {
            deleteQuietly(originalKey, processedKey);

            throw new BusinessException(
                    ErrorCode.UNSUPPORTED_IMAGE_TYPE
            );
        }
    }

    private void validateKeyPair(
            String originalKey,
            String processedKey,
            String requiredCategory
    ) {
        Matcher originalMatcher =
                ORIGINAL_KEY_PATTERN.matcher(originalKey);

        Matcher processedMatcher =
                PROCESSED_KEY_PATTERN.matcher(processedKey);

        if (!originalMatcher.matches()
                || !processedMatcher.matches()) {
            throw new BusinessException(
                    ErrorCode.INVALID_IMAGE_UPLOAD_REQUEST
            );
        }

        String originalCategory = originalMatcher.group(1);
        String originalIdentifier = originalMatcher.group(2);

        String processedCategory = processedMatcher.group(1);
        String processedIdentifier = processedMatcher.group(2);

        if (!originalCategory.equals(processedCategory)
                || !originalIdentifier.equals(processedIdentifier)) {
            throw new BusinessException(
                    ErrorCode.INVALID_IMAGE_UPLOAD_REQUEST
            );
        }

        if (requiredCategory != null
                && !requiredCategory.equals(originalCategory)) {
            throw new BusinessException(
                    ErrorCode.INVALID_IMAGE_UPLOAD_REQUEST
            );
        }
    }

    private void deleteQuietly(
            String originalKey,
            String processedKey
    ) {
        try {
            deleteObject(
                    awsProperties.s3().originalBucket(),
                    originalKey
            );

            deleteObject(
                    awsProperties.s3().processedBucket(),
                    processedKey
            );
        } catch (SdkException ignored) {
        }
    }

    private void deleteObject(
            String bucket,
            String key
    ) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );
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
