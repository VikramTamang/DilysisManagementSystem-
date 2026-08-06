package com.fonepay.gateway.controller;

import com.fonepay.gateway.constant.ApiConstants;
import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.LoginRequest;
import com.fonepay.gateway.dto.request.PatientRegistrationRequest;
import com.fonepay.gateway.dto.response.LoginResponse;
import com.fonepay.gateway.dto.response.PatientResponse;
import com.fonepay.gateway.user.service.auth.LoginService;
import com.fonepay.gateway.user.service.patient.CreatePatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final CreatePatientService createPatientService;

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

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<PatientResponse>> register(
            @Valid @RequestBody PatientRegistrationRequest request) {

        PatientResponse created = createPatientService.registerSelf(request);

        ApiResponse<PatientResponse> response = ApiResponse.<PatientResponse>builder()
                .success(true)
                .message("Registration successful. You can now log in.")
                .data(created)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}