package com.fonepay.gateway.controller;

import com.fonepay.gateway.appointment.service.machine.MachineService;
import com.fonepay.gateway.constant.ApiConstants;
import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.MachineRequest;
import com.fonepay.gateway.dto.request.MachineStatusRequest;
import com.fonepay.gateway.dto.response.MachineResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.Machine.BASE)
@RequiredArgsConstructor
public class MachineController {

    private final MachineService machineService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<MachineResponse>> createMachine(@Valid @RequestBody MachineRequest request) {
        MachineResponse responseData = machineService.createMachine(request);

        ApiResponse<MachineResponse> response = ApiResponse.<MachineResponse>builder()
                .success(true)
                .message("Machine created successfully")
                .data(responseData)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<MachineResponse>>> getAllMachines() {
        List<MachineResponse> list = machineService.getAllMachines();

        ApiResponse<List<MachineResponse>> response = ApiResponse.<List<MachineResponse>>builder()
                .success(true)
                .message("Machines retrieved successfully")
                .data(list)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MachineResponse>> getMachineById(@PathVariable Long id) {
        MachineResponse responseData = machineService.getMachineById(id);

        ApiResponse<MachineResponse> response = ApiResponse.<MachineResponse>builder()
                .success(true)
                .message("Machine retrieved successfully")
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MachineResponse>> updateMachine(
            @PathVariable Long id,
            @Valid @RequestBody MachineRequest request
    ) {
        MachineResponse responseData = machineService.updateMachine(id, request);

        ApiResponse<MachineResponse> response = ApiResponse.<MachineResponse>builder()
                .success(true)
                .message("Machine updated successfully")
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<MachineResponse>> updateMachineStatus(
            @PathVariable Long id,
            @Valid @RequestBody MachineStatusRequest request
    ) {
        MachineResponse responseData = machineService.updateMachineStatus(id, request);

        ApiResponse<MachineResponse> response = ApiResponse.<MachineResponse>builder()
                .success(true)
                .message("Machine status updated successfully")
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMachine(@PathVariable Long id) {
        machineService.deleteMachine(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Machine deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }
}