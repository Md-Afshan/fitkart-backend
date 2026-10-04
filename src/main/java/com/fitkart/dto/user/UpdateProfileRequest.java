package com.fitkart.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Phone number must be 10 digits"
    )
    private String phone;
}