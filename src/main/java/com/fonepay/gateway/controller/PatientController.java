package com.fonepay.gateway.controller;

import com.fonepay.gateway.constant.ApiConstants;
import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.PatientRequest;
import com.fonepay.gateway.dto.response.PatientResponse;
import com.fonepay.gateway.entity.enums.Role;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.user.service.patient.*;
import com.fonepay.gateway.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.Patient.BASE)
@RequiredArgsConstructor
public class PatientController {

    private final CreatePatientService createPatientService;
    private final UpdatePatientService updatePatientService;
    private final DeletePatientService deletePatientService;
    private final GetPatientService getPatientService;
    private final GetAllPatientsService getAllPatientsService;

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @PostMapping
    public ResponseEntity<ApiResponse<PatientResponse>> createPatient(
            @Valid @RequestBody PatientRequest request) {

        PatientResponse created = createPatientService.createPatient(request);

        ApiResponse<PatientResponse> response = ApiResponse.<PatientResponse>builder()
                .success(true)
                .message("Patient created successfully")
                .data(created)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser.getRole() == Role.PATIENT && !currentUser.getId().equals(id)) {
            throw new AppException("Access denied: You can only update your own profile.", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }

        PatientResponse updated = updatePatientService.updatePatient(id, request);

        ApiResponse<PatientResponse> response = ApiResponse.<PatientResponse>builder()
                .success(true)
                .message("Patient updated successfully")
                .data(updated)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable Long id) {
        deletePatientService.deletePatient(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Patient deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser.getRole() == Role.PATIENT && !currentUser.getId().equals(id)) {
            throw new AppException("Access denied: You can only view your own profile.", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }

        PatientResponse patient = getPatientService.getPatientById(id);

        ApiResponse<PatientResponse> response = ApiResponse.<PatientResponse>builder()
                .success(true)
                .message("Patient retrieved successfully")
                .data(patient)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientResponse>>> getAllPatients() {
        List<PatientResponse> list = getAllPatientsService.getAllPatients();

        ApiResponse<List<PatientResponse>> response = ApiResponse.<List<PatientResponse>>builder()
                .success(true)
                .message("Patients retrieved successfully")
                .data(list)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PatientResponse>> getCurrentPatient(
            @AuthenticationPrincipal User currentUser) {

        PatientResponse patient = getPatientService.getPatientById(currentUser.getId());

        ApiResponse<PatientResponse> response = ApiResponse.<PatientResponse>builder()
                .success(true)
                .message("Patient profile retrieved successfully")
                .data(patient)
                .build();

        return ResponseEntity.ok(response);
    }
}