package com.fonepay.gateway.controller;

import com.fonepay.gateway.constant.ApiConstants;
import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.NurseRequest;
import com.fonepay.gateway.dto.response.NurseResponse;
import com.fonepay.gateway.user.entity.User;
import com.fonepay.gateway.user.service.NurseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.Nurse.BASE)
@RequiredArgsConstructor
public class NurseController {

    private final NurseService nurseService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<NurseResponse>> createNurse(@Valid @RequestBody NurseRequest request) {
        NurseResponse responseData = nurseService.createNurse(request);

        ApiResponse<NurseResponse> response = ApiResponse.<NurseResponse>builder()
                .success(true)
                .message("Nurse account created successfully")
                .data(responseData)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NurseResponse>> updateNurse(
            @PathVariable Long id,
            @Valid @RequestBody NurseRequest request) {

        NurseResponse responseData = nurseService.updateNurse(id, request);

        ApiResponse<NurseResponse> response = ApiResponse.<NurseResponse>builder()
                .success(true)
                .message("Nurse account updated successfully")
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNurse(@PathVariable Long id) {
        nurseService.deleteNurse(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Nurse account deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NurseResponse>> getNurseById(@PathVariable Long id) {
        NurseResponse responseData = nurseService.getNurseById(id);

        ApiResponse<NurseResponse> response = ApiResponse.<NurseResponse>builder()
                .success(true)
                .message("Nurse retrieved successfully")
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NurseResponse>>> getAllNurses() {
        List<NurseResponse> list = nurseService.getAllNurses();

        ApiResponse<List<NurseResponse>> response = ApiResponse.<List<NurseResponse>>builder()
                .success(true)
                .message("Nurses retrieved successfully")
                .data(list)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('NURSE')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<NurseResponse>> getCurrentNurse(@AuthenticationPrincipal User currentUser) {
        NurseResponse responseData = nurseService.getNurseById(currentUser.getId());

        ApiResponse<NurseResponse> response = ApiResponse.<NurseResponse>builder()
                .success(true)
                .message("Nurse profile retrieved successfully")
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }
}
