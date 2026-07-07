package com.fonepay.gateway.service.auth;

import com.fonepay.gateway.dto.request.LoginRequest;
import com.fonepay.gateway.dto.response.LoginResponse;
import com.fonepay.gateway.entity.User;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public LoginResponse authenticate(LoginRequest request) {
        log.info("Processing login request for username: {}", request.getUsername());

        // Find user in the database by username (email)
        User user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new AppException("Invalid credentials", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"));

        // Verify hashed password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException("Invalid credentials", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }

        return LoginResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
