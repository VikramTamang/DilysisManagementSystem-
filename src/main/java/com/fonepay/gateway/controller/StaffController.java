package com.fonepay.gateway.controller;

import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.StaffRequest;
import com.fonepay.gateway.dto.response.StaffResponse;
import com.fonepay.gateway.service.staff.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.fonepay.gateway.constant.ApiConstants;
import com.fonepay.gateway.entity.User;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.Staff.BASE)
@RequiredArgsConstructor
public class StaffController {

    private final CreateStaffService createStaffService;
    private final GetStaffService getStaffService;
    private final GetAllStaffService getAllStaffService;
    private final UpdateStaffService updateStaffService;
    private final DeleteStaffService deleteStaffService;

    @PreAuthorize("hasRole('ADMIN')") // only admins hire staff
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

    // Any logged-in staff member can fetch their own info — no need to know
    // their own database ID. authentication.principal is resolved by
    // CustomUserDetailsService from the Basic Auth credentials on this request.
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StaffResponse>> getMyInfo(
            @AuthenticationPrincipal User currentUser) {

        StaffResponse staff = getStaffService.getStaffById(currentUser.getId());

        ApiResponse<StaffResponse> response = ApiResponse.<StaffResponse>builder()
                .success(true)
                .message("Staff retrieved successfully")
                .data(staff)
                .build();

        return ResponseEntity.ok(response);
    }

    // ADMIN can view any staff record; a STAFF member can only view their own
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffResponse>> getStaffById(@P("id") @PathVariable Long id) {
        StaffResponse staff = getStaffService.getStaffById(id);

        ApiResponse<StaffResponse> response = ApiResponse.<StaffResponse>builder()
                .success(true)
                .message("Staff retrieved successfully")
                .data(staff)
                .build();

        return ResponseEntity.ok(response);
    }

    // Only admins should see the full staff roster
    @PreAuthorize("hasRole('ADMIN')")
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

    @PreAuthorize("hasRole('ADMIN')")
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

    @PreAuthorize("hasRole('ADMIN')")
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