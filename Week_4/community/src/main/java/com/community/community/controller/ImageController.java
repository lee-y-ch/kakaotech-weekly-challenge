package com.community.community.controller;

import com.community.community.ApiResponse;
import com.community.community.auth.CurrentUserId;
import com.community.community.dto.ImageUploadResponseDTO;
import com.community.community.dto.PresignedImageUploadRequestDTO;
import com.community.community.dto.PresignedImageUploadResponseDTO;
import com.community.community.dto.SignupProfileImageUploadRequestDTO;
import com.community.community.service.ImagePresignedUrlService;
import com.community.community.service.ImageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImageController {

    private final ImageService imageService;
    private final ImagePresignedUrlService imagePresignedUrlService;

    public ImageController(ImageService imageService, ImagePresignedUrlService imagePresignedUrlService) {
        this.imageService = imageService;
        this.imagePresignedUrlService = imagePresignedUrlService;
    }

    @PostMapping(value = "/images", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadImage(
            @RequestPart("image") MultipartFile image,
            @RequestParam("type") String type
    ) {
        ImageUploadResponseDTO data = imageService.uploadImage(image, type);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("image_upload_success", data)
        );
    }

    @PostMapping("/images/presigned-url")
    public ResponseEntity<?> createPresignedImageUploadUrl(
            // currentUserId는 로직에서 사용되는 값이 아닌, 인증 통과를 위한 값
            @CurrentUserId int currentUserId,
            @Valid @RequestBody
            PresignedImageUploadRequestDTO request
    ) {
        PresignedImageUploadResponseDTO data =
                imagePresignedUrlService
                        .createAuthenticatedUploadUrl(request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "presigned_url_create_success",
                        data
                )
        );
    }

    @PostMapping("/images/presigned-url/signup-profile")
    public ResponseEntity<?> createSignupProfileUploadUrl(
            @Valid @RequestBody
            SignupProfileImageUploadRequestDTO request
    ) {
        PresignedImageUploadResponseDTO data =
                imagePresignedUrlService
                        .createSignupProfileUploadUrl(request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "presigned_url_create_success",
                        data
                )
        );
    }
}