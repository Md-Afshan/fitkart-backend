package com.fitkart.dto.user;

import com.fitkart.entity.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {

    private Long id;

    private String fullName;

    private String email;

    private String phone;

    private Role role;
}