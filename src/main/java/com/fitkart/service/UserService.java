package com.fitkart.service;

import com.fitkart.dto.user.AuthResponse;
import com.fitkart.dto.user.LoginRequest;
import com.fitkart.dto.user.RegisterRequest;
import com.fitkart.dto.user.UserResponse;

public interface UserService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getProfile(String email);
}