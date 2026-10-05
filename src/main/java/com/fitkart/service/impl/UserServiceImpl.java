package com.fitkart.service.impl;

import com.fitkart.dto.user.AuthResponse;
import com.fitkart.dto.user.ChangePasswordRequest;
import com.fitkart.dto.user.LoginRequest;
import com.fitkart.dto.user.RegisterRequest;
import com.fitkart.dto.user.UpdateProfileRequest;
import com.fitkart.dto.user.UserResponse;
import com.fitkart.entity.Cart;
import com.fitkart.entity.Role;
import com.fitkart.entity.User;
import com.fitkart.exception.InvalidCredentialsException;
import com.fitkart.exception.ResourceAlreadyExistsException;
import com.fitkart.repository.CartRepository;
import com.fitkart.repository.UserRepository;
import com.fitkart.security.service.JwtService;
import com.fitkart.service.UserService;
import com.fitkart.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
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

        /*
         * Create an empty cart for the newly registered customer.
         */
        Cart cart = new Cart();
        cart.setUser(savedUser);
        cart.setCreatedAt(LocalDateTime.now());

        cartRepository.save(cart);

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

    @Override
    public UserResponse updateProfile(
            String email,
            UpdateProfileRequest request
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Authenticated user not found."
                        )
                );

        if (!user.getPhone().equals(request.getPhone())) {

            userRepository.findByPhone(request.getPhone())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(user.getId())) {
                            throw new ResourceAlreadyExistsException(
                                    "Phone number is already registered."
                            );
                        }
                    });
        }

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        User updatedUser = userRepository.save(user);

        return UserMapper.toResponse(updatedUser);
    }

    @Override
    public void changePassword(
            String email,
            ChangePasswordRequest request
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Authenticated user not found."
                        )
                );

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword()
        )) {
            throw new InvalidCredentialsException(
                    "Current password is incorrect."
            );
        }

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);
    }
    @Override
    @Transactional(readOnly = true)
    public java.util.List<UserResponse> getCustomers(String search) {

        if (search == null || search.isBlank()) {

            return userRepository
                    .findByRoleOrderByCreatedAtDesc(Role.CUSTOMER)
                    .stream()
                    .map(UserMapper::toResponse)
                    .toList();
        }

        String searchValue = search.trim();

        return userRepository
                .findByRoleAndFullNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
                        Role.CUSTOMER,
                        searchValue,
                        Role.CUSTOMER,
                        searchValue
                )
                .stream()
                .map(UserMapper::toResponse)
                .toList();
    }
}

