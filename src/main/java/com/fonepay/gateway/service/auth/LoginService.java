package com.fonepay.gateway.service.auth;

import com.fonepay.gateway.dto.request.LoginRequest;
import com.fonepay.gateway.dto.response.LoginResponse;
import com.fonepay.gateway.entity.Staff;
import com.fonepay.gateway.entity.User;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

import com.fonepay.gateway.dto.token.RefreshTokenRequest;
import com.fonepay.gateway.dto.token.RefreshTokenResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse authenticate(LoginRequest request) {
        log.info("Processing login request for username: {}", request.getUsername());

        // Find user in the database by username (email)
        User user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new AppException("Invalid credentials", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"));

        // Verify hashed password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException("Invalid credentials", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }

        // Staff have a designation (Doctor, Nurse, etc.) that plain STAFF role doesn't capture
        String designation = (user instanceof Staff staff) ? staff.getDesignation() : null;

        // Generate Access and Refresh Tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return LoginResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .designation(designation)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        try {
            String username = jwtService.extractUsername(token);
            String tokenType = jwtService.extractTokenType(token);

            if (!"REFRESH".equals(tokenType)) {
                throw new AppException("Invalid token type", HttpStatus.BAD_REQUEST, "INVALID_TOKEN_TYPE");
            }

            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

            if (jwtService.isTokenValid(token, user)) {
                String newAccessToken = jwtService.generateAccessToken(user);
                String newRefreshToken = jwtService.generateRefreshToken(user);

                return RefreshTokenResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .build();
            } else {
                throw new AppException("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN");
            }
        } catch (Exception e) {
            if (e instanceof AppException) {
                throw (AppException) e;
            }
            throw new AppException("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN");
        }
    }
}