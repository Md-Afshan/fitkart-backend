package com.fitkart.service;

import com.fitkart.dto.user.AuthResponse;
import com.fitkart.dto.user.ChangePasswordRequest;
import com.fitkart.dto.user.LoginRequest;
import com.fitkart.dto.user.RegisterRequest;
import com.fitkart.dto.user.UpdateProfileRequest;
import com.fitkart.dto.user.UserResponse;

public interface UserService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getProfile(String email);

    UserResponse updateProfile(String email, UpdateProfileRequest request);

    void changePassword(String email, ChangePasswordRequest request);

    java.util.List<UserResponse> getCustomers(String search);
}
