package com.fitkart.repository;

import com.fitkart.entity.Role;
import com.fitkart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<User> findByRoleOrderByCreatedAtDesc(Role role);

    List<User> findByRoleAndFullNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
            Role roleForName,
            String name,
            Role roleForEmail,
            String email
    );
}
