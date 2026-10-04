package com.fitkart.controller;

import com.fitkart.dto.user.ChangePasswordRequest;
import com.fitkart.dto.user.UpdateProfileRequest;
import com.fitkart.dto.user.UserResponse;
import com.fitkart.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public UserResponse getProfile(Authentication authentication) {

        String email = authentication.getName();

        return userService.getProfile(email);
    }

    @PutMapping("/profile")
    public UserResponse updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {

        String email = authentication.getName();

        return userService.updateProfile(email, request);
    }

    @PutMapping("/password")
    public void changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {

        String email = authentication.getName();

        userService.changePassword(email, request);
    }
}