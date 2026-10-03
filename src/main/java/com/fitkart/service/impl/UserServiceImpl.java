package com.fitkart.service.impl;

import com.fitkart.dto.user.AuthResponse;
import com.fitkart.dto.user.LoginRequest;
import com.fitkart.dto.user.RegisterRequest;
import com.fitkart.dto.user.UserResponse;
import com.fitkart.entity.Role;
import com.fitkart.entity.User;
import com.fitkart.exception.InvalidCredentialsException;
import com.fitkart.exception.ResourceAlreadyExistsException;
import com.fitkart.repository.UserRepository;
import com.fitkart.security.service.JwtService;
import com.fitkart.service.UserService;
import com.fitkart.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException(
                    "Email is already registered."
            );
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ResourceAlreadyExistsException(
                    "Phone number is already registered."
            );
        }

        User user = UserMapper.toEntity(request);

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(Role.CUSTOMER);

        User savedUser = userRepository.save(user);

        return UserMapper.toResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or password."
                        )
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new InvalidCredentialsException(
                    "Invalid email or password."
            );
        }

        String token = jwtService.generateToken(
                user.getEmail()
        );

        return new AuthResponse(
                "Login successful.",
                token
        );
    }

    @Override
    public UserResponse getProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Authenticated user not found."
                        )
                );

        return UserMapper.toResponse(user);
    }
}