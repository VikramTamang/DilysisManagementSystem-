package com.fonepay.gateway.controller;

import com.fonepay.gateway.constant.ApiConstants;
import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.LoginRequest;
import com.fonepay.gateway.dto.response.LoginResponse;
import com.fonepay.gateway.user.service.auth.LoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.Auth.BASE)
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse loginResponse = loginService.authenticate(request);

        ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Login successful")
                .data(loginResponse)
                .build();

        return ResponseEntity.ok(response);
    }
}
