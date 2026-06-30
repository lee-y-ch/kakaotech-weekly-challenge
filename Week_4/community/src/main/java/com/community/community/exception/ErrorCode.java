package com.community.community.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // User
    INVALID_REGISTER_REQUEST(HttpStatus.BAD_REQUEST, "invalid_register_request"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "email_already_exists"),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "nickname_already_exists"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "user_not_found"),
    INVALID_UPDATE_USER_REQUEST(HttpStatus.BAD_REQUEST, "invalid_update_user_request"),
    INVALID_UPDATE_PASSWORD_REQUEST(HttpStatus.UNPROCESSABLE_ENTITY, "invalid_update_password_request"),
    INVALID_NICKNAME_CHECK_REQUEST(HttpStatus.BAD_REQUEST, "invalid_nickname_check_request"),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "duplicate_nickname"),
    INVALID_EMAIL_CHECK_REQUEST(HttpStatus.BAD_REQUEST, "invalid_email_check_request"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "duplicate_email"),

    // Post
    INVALID_CREATE_POST_REQUEST(HttpStatus.BAD_REQUEST, "invalid_create_post_request"),
    INVALID_POSTS_REQUEST(HttpStatus.BAD_REQUEST, "invalid_posts_request"),
    INVALID_UPDATE_POST_REQUEST(HttpStatus.BAD_REQUEST, "invalid_update_post_request"),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "post_not_found"),
    POST_LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "post_like_not_found"),

    // Image
    INVALID_IMAGE_UPLOAD_REQUEST(HttpStatus.BAD_REQUEST, "invalid_image_upload_request"),
    UNSUPPORTED_IMAGE_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "unsupported_image_type"),
    IMAGE_FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "image_file_too_large"),
    PRESIGNED_URL_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "presigned_url_creation_failed"),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "image_not_found"),
    IMAGE_STATUS_CHECK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "image_status_check_failed"),
    IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "image_delete_failed"),

    // Comment
    INVALID_CREATE_COMMENT_REQUEST(HttpStatus.BAD_REQUEST, "invalid_create_comment_request"),
    INVALID_COMMENTS_REQUEST(HttpStatus.BAD_REQUEST, "invalid_comments_request"),
    INVALID_UPDATE_COMMENT_REQUEST(HttpStatus.UNPROCESSABLE_ENTITY, "invalid_update_comment_request"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "comment_not_found"),

    // Auth
    INVALID_LOGIN_REQUEST(HttpStatus.BAD_REQUEST, "invalid_login_request"),
    INVALID_EMAIL_OR_PASSWORD(HttpStatus.UNAUTHORIZED, "invalid_email_or_password"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "forbidden"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error");


    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
