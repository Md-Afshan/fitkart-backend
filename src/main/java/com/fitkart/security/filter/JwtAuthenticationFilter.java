package com.fitkart.security.filter;

import com.fitkart.entity.User;
import com.fitkart.repository.UserRepository;
import com.fitkart.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            String username = jwtService.extractUsername(token);

            if (username != null) {

                User user = userRepository.findByEmail(username)
                        .orElse(null);

                if (user != null) {

                    boolean tokenValid = jwtService.isTokenValid(
                            token,
                            user.getEmail()
                    );

                    if (tokenValid
                            && SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null) {

                        SimpleGrantedAuthority authority =
                                new SimpleGrantedAuthority(
                                        "ROLE_" + user.getRole().name()
                                );

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        user.getEmail(),
                                        null,
                                        List.of(authority)
                                );

                        SecurityContextHolder
                                .getContext()
                                .setAuthentication(authentication);
                    }
                }
            }

        } catch (Exception exception) {

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}