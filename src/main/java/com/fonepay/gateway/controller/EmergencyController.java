package com.fonepay.gateway.controller;

import com.fonepay.gateway.appointment.service.emergency.EmergencyReassignmentService;
import com.fonepay.gateway.constant.ApiConstants;
import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.StaffUnavailabilityRequest;
import com.fonepay.gateway.dto.response.EmergencyReassignmentResponse;
import com.fonepay.gateway.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.Emergency.BASE)
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyReassignmentService emergencyReassignmentService;

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @PostMapping("/staff-unavailable")
    public ResponseEntity<ApiResponse<EmergencyReassignmentResponse>> handleStaffUnavailability(
            @Valid @RequestBody StaffUnavailabilityRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        EmergencyReassignmentResponse result = emergencyReassignmentService.handleStaffUnavailability(
                request,
                currentUser.getId(),
                currentUser.getRole().name()
        );

        ApiResponse<EmergencyReassignmentResponse> response = ApiResponse.<EmergencyReassignmentResponse>builder()
                .success(true)
                .message("Affected appointments processed")
                .data(result)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
