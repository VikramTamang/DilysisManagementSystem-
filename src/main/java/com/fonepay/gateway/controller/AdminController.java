package com.fonepay.gateway.controller;

import com.fonepay.gateway.constant.ApiConstants;
import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.AdminRequest;
import com.fonepay.gateway.dto.response.AdminResponse;
import com.fonepay.gateway.service.admin.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.Admin.BASE)
@RequiredArgsConstructor
public class AdminController {

    private final CreateAdminService createAdminService;
    private final GetAdminService getAdminService;
    private final GetAllAdminsService getAllAdminsService;
    private final UpdateAdminService updateAdminService;
    private final DeleteAdminService deleteAdminService;

    @PostMapping
    public ResponseEntity<ApiResponse<AdminResponse>> createAdmin(
            @Valid @RequestBody AdminRequest request) {

        AdminResponse created = createAdminService.createAdmin(request);

        ApiResponse<AdminResponse> response = ApiResponse.<AdminResponse>builder()
                .success(true)
                .message("Admin created successfully")
                .data(created)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminResponse>> getAdminById(@PathVariable Long id) {
        AdminResponse admin = getAdminService.getAdminById(id);

        ApiResponse<AdminResponse> response = ApiResponse.<AdminResponse>builder()
                .success(true)
                .message("Admin retrieved successfully")
                .data(admin)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminResponse>>> getAllAdmins() {
        List<AdminResponse> admins = getAllAdminsService.getAllAdmins();

        ApiResponse<List<AdminResponse>> response = ApiResponse.<List<AdminResponse>>builder()
                .success(true)
                .message("Admins retrieved successfully")
                .data(admins)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminResponse>> updateAdmin(
            @PathVariable Long id,
            @Valid @RequestBody AdminRequest request) {

        AdminResponse updated = updateAdminService.updateAdmin(id, request);

        ApiResponse<AdminResponse> response = ApiResponse.<AdminResponse>builder()
                .success(true)
                .message("Admin updated successfully")
                .data(updated)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(@PathVariable Long id) {
        deleteAdminService.deleteAdmin(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Admin deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }
}
