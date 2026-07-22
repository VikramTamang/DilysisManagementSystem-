package com.fonepay.gateway.user.service.auth;

import com.fonepay.gateway.dto.request.LoginRequest;
import com.fonepay.gateway.dto.response.LoginResponse;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.user.entity.User;
import com.fonepay.gateway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse authenticate(LoginRequest request) {
        log.info("Processing login request for username: {}", request.getUsername());

        User user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new AppException("Invalid credentials", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException("Invalid credentials", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }

        String accessToken = jwtService.generateAccessToken(user);

        return LoginResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .build();
    }
}