package com.fonepay.gateway.controller;

import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.StaffRequest;
import com.fonepay.gateway.dto.response.StaffResponse;
import com.fonepay.gateway.service.staff.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffController {

    private final CreateStaffService createStaffService;
    private final GetStaffService getStaffService;
    private final GetAllStaffService getAllStaffService;
    private final UpdateStaffService updateStaffService;
    private final DeleteStaffService deleteStaffService;

    @PostMapping
    public ResponseEntity<ApiResponse<StaffResponse>> createStaff(
            @Valid @RequestBody StaffRequest request) {

        StaffResponse created = createStaffService.createStaff(request);

        ApiResponse<StaffResponse> response = ApiResponse.<StaffResponse>builder()
                .success(true)
                .message("Staff created successfully")
                .data(created)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffResponse>> getStaffById(@PathVariable Long id) {
        StaffResponse staff = getStaffService.getStaffById(id);

        ApiResponse<StaffResponse> response = ApiResponse.<StaffResponse>builder()
                .success(true)
                .message("Staff retrieved successfully")
                .data(staff)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getAllStaff() {
        List<StaffResponse> staffList = getAllStaffService.getAllStaff();

        ApiResponse<List<StaffResponse>> response = ApiResponse.<List<StaffResponse>>builder()
                .success(true)
                .message("Staff retrieved successfully")
                .data(staffList)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffResponse>> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody StaffRequest request) {

        StaffResponse updated = updateStaffService.updateStaff(id, request);

        ApiResponse<StaffResponse> response = ApiResponse.<StaffResponse>builder()
                .success(true)
                .message("Staff updated successfully")
                .data(updated)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(@PathVariable Long id) {
        deleteStaffService.deleteStaff(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Staff deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }
}
