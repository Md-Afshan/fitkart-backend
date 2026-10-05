package com.fitkart.controller;

import com.fitkart.dto.user.UserResponse;
import com.fitkart.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomerController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getCustomers(
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(
                userService.getCustomers(search)
        );
    }
}
