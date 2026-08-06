package com.fonepay.gateway.controller;

import com.fonepay.gateway.constant.ApiConstants;
import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.DoctorRequest;
import com.fonepay.gateway.dto.request.StaffStatusRequest;
import com.fonepay.gateway.dto.response.DoctorResponse;
import com.fonepay.gateway.user.entity.User;
import com.fonepay.gateway.user.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.Doctor.BASE)
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<DoctorResponse>> createDoctor(@Valid @RequestBody DoctorRequest request) {
        DoctorResponse responseData = doctorService.createDoctor(request);

        ApiResponse<DoctorResponse> response = ApiResponse.<DoctorResponse>builder()
                .success(true)
                .message("Doctor account created successfully")
                .data(responseData)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorResponse>> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequest request) {

        DoctorResponse responseData = doctorService.updateDoctor(id, request);

        ApiResponse<DoctorResponse> response = ApiResponse.<DoctorResponse>builder()
                .success(true)
                .message("Doctor account updated successfully")
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DoctorResponse>> updateDoctorStatus(
            @PathVariable Long id,
            @Valid @RequestBody StaffStatusRequest request) {

        DoctorResponse responseData = doctorService.updateDoctorStatus(id, request.getAccountStatus());

        ApiResponse<DoctorResponse> response = ApiResponse.<DoctorResponse>builder()
                .success(true)
                .message("Doctor account status updated to " + request.getAccountStatus())
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Doctor account deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorResponse>> getDoctorById(@PathVariable Long id) {
        DoctorResponse responseData = doctorService.getDoctorById(id);

        ApiResponse<DoctorResponse> response = ApiResponse.<DoctorResponse>builder()
                .success(true)
                .message("Doctor retrieved successfully")
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getAllDoctors() {
        List<DoctorResponse> list = doctorService.getAllDoctors();

        ApiResponse<List<DoctorResponse>> response = ApiResponse.<List<DoctorResponse>>builder()
                .success(true)
                .message("Doctors retrieved successfully")
                .data(list)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<DoctorResponse>> getCurrentDoctor(@AuthenticationPrincipal User currentUser) {
        DoctorResponse responseData = doctorService.getDoctorById(currentUser.getId());

        ApiResponse<DoctorResponse> response = ApiResponse.<DoctorResponse>builder()
                .success(true)
                .message("Doctor profile retrieved successfully")
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }
}