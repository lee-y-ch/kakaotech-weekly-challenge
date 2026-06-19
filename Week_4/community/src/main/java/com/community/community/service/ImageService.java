package com.community.community.service;

import com.community.community.dto.ImageUploadResponseDTO;
import com.community.community.exception.BusinessException;
import com.community.community.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageService {

    private final Path uploadRootPath;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    public ImageService(@Value("${file.upload-dir}") String uploadDir) {
        this.uploadRootPath = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public ImageUploadResponseDTO uploadImage(MultipartFile image, String type) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_UPLOAD_REQUEST);
        }

        if (type == null || type.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_UPLOAD_REQUEST);
        }

        if (!type.equals("profile") && !type.equals("post")) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_UPLOAD_REQUEST);
        }

        String contentType = image.getContentType();

        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
        }

        try {
            Path uploadPath = uploadRootPath.resolve(type);
            Files.createDirectories(uploadPath);

            String originalFilename = image.getOriginalFilename();
            String extension = getExtension(originalFilename);
            String storedFilename = UUID.randomUUID() + extension;

            // MultipartFile을 실제 저장 경로에 복사하고, 이후 접근 가능한 이미지 URL만 응답으로 반환한다.
            Path filePath = uploadPath.resolve(storedFilename).normalize();

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 로컬 호스트를 포함하지 않는 상대 경로를 저장하도록 수정.
            String imageUrl = "/images/" + type + "/" + storedFilename;

            return new ImageUploadResponseDTO(imageUrl);

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }

        return filename.substring(filename.lastIndexOf("."));
    }
}