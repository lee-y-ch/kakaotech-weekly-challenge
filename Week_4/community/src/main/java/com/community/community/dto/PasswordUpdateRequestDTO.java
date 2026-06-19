package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PasswordUpdateRequestDTO {

    @NotBlank(message = "invalid_update_password_request")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
            message = "invalid_update_password_request"
    )
    private String password;

    @JsonProperty("password_confirm")
    @NotBlank(message = "invalid_update_password_request")
    private String passwordConfirm;
}
