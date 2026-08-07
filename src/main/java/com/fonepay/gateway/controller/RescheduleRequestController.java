package com.fonepay.gateway.controller;

import com.fonepay.gateway.appointment.service.reschedulerequest.RescheduleRequestService;
import com.fonepay.gateway.constant.ApiConstants;
import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.RescheduleRequestCreateRequest;
import com.fonepay.gateway.dto.request.RescheduleRequestReviewRequest;
import com.fonepay.gateway.dto.response.RescheduleRequestResponse;
import com.fonepay.gateway.entity.enums.Role;
import com.fonepay.gateway.entity.enums.RescheduleRequestStatus;
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
@RequestMapping(ApiConstants.RescheduleRequest.BASE)
@RequiredArgsConstructor
public class RescheduleRequestController {

    private final RescheduleRequestService rescheduleRequestService;

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public ResponseEntity<ApiResponse<RescheduleRequestResponse>> createRequest(
            @Valid @RequestBody RescheduleRequestCreateRequest request,
            @AuthenticationPrincipal User currentUser) {

        RescheduleRequestResponse responseData = rescheduleRequestService.createRequest(request, currentUser.getId());

        ApiResponse<RescheduleRequestResponse> response = ApiResponse.<RescheduleRequestResponse>builder()
                .success(true)
                .message("Reschedule request submitted. Staff will review it shortly.")
                .data(responseData)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RescheduleRequestResponse>>> getAllRequests(
            @RequestParam(required = false) RescheduleRequestStatus status,
            @AuthenticationPrincipal User currentUser) {

        List<RescheduleRequestResponse> list;
        if (currentUser.getRole() == Role.PATIENT) {
            list = rescheduleRequestService.getRequestsForPatient(currentUser.getId());
        } else {
            list = rescheduleRequestService.getAllRequests(status);
        }

        ApiResponse<List<RescheduleRequestResponse>> response = ApiResponse.<List<RescheduleRequestResponse>>builder()
                .success(true)
                .message("Reschedule requests retrieved successfully")
                .data(list)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RescheduleRequestResponse>> getRequestById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        boolean isPatientRole = currentUser.getRole() == Role.PATIENT;
        RescheduleRequestResponse responseData = rescheduleRequestService.getRequestById(id, currentUser.getId(), isPatientRole);

        ApiResponse<RescheduleRequestResponse> response = ApiResponse.<RescheduleRequestResponse>builder()
                .success(true)
                .message("Reschedule request retrieved successfully")
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<RescheduleRequestResponse>> approveRequest(
            @PathVariable Long id,
            @RequestBody(required = false) RescheduleRequestReviewRequest request,
            @AuthenticationPrincipal User currentUser) {

        RescheduleRequestReviewRequest reviewRequest = request != null ? request : RescheduleRequestReviewRequest.builder().build();

        RescheduleRequestResponse responseData = rescheduleRequestService.approveRequest(
                id, reviewRequest, currentUser.getId(), currentUser.getRole().name());

        ApiResponse<RescheduleRequestResponse> response = ApiResponse.<RescheduleRequestResponse>builder()
                .success(true)
                .message("Reschedule request approved and appointment updated")
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<RescheduleRequestResponse>> rejectRequest(
            @PathVariable Long id,
            @RequestBody(required = false) RescheduleRequestReviewRequest request,
            @AuthenticationPrincipal User currentUser) {

        RescheduleRequestReviewRequest reviewRequest = request != null ? request : RescheduleRequestReviewRequest.builder().build();

        RescheduleRequestResponse responseData = rescheduleRequestService.rejectRequest(id, reviewRequest, currentUser.getId());

        ApiResponse<RescheduleRequestResponse> response = ApiResponse.<RescheduleRequestResponse>builder()
                .success(true)
                .message("Reschedule request rejected")
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }
}