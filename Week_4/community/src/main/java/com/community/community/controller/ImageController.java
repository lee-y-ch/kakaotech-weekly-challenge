package com.community.community.controller;

import com.community.community.ApiResponse;
import com.community.community.dto.ImageUploadResponseDTO;
import com.community.community.service.ImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
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
}
