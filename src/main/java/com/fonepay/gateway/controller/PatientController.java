package com.fonepay.gateway.controller;

import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.PatientRequest;
import com.fonepay.gateway.dto.response.PatientResponse;
import com.fonepay.gateway.service.patient.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.fonepay.gateway.constant.ApiConstants;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.Patient.BASE)
@RequiredArgsConstructor
public class PatientController {

    private final CreatePatientService createPatientService;
    private final GetPatientService getPatientService;
    private final GetAllPatientsService getAllPatientsService;
    private final UpdatePatientService updatePatientService;
    private final DeletePatientService deletePatientService;

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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(@PathVariable Long id) {
        PatientResponse patient = getPatientService.getPatientById(id);

        ApiResponse<PatientResponse> response = ApiResponse.<PatientResponse>builder()
                .success(true)
                .message("Patient retrieved successfully")
                .data(patient)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientResponse>>> getAllPatients() {
        List<PatientResponse> patients = getAllPatientsService.getAllPatients();

        ApiResponse<List<PatientResponse>> response = ApiResponse.<List<PatientResponse>>builder()
                .success(true)
                .message("Patients retrieved successfully")
                .data(patients)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request) {

        PatientResponse updated = updatePatientService.updatePatient(id, request);

        ApiResponse<PatientResponse> response = ApiResponse.<PatientResponse>builder()
                .success(true)
                .message("Patient updated successfully")
                .data(updated)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable Long id) {
        deletePatientService.deletePatient(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Patient deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }
}
