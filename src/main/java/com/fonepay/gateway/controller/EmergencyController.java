package com.fonepay.gateway.controller;

import com.fonepay.gateway.appointment.service.appointment.CreateAppointmentService;
import com.fonepay.gateway.appointment.service.appointment.GetAllAppointmentsService;
import com.fonepay.gateway.appointment.service.emergency.EmergencyReassignmentService;
import com.fonepay.gateway.constant.ApiConstants;
import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.AppointmentRequest;
import com.fonepay.gateway.dto.request.StaffUnavailabilityRequest;
import com.fonepay.gateway.dto.response.AppointmentResponse;
import com.fonepay.gateway.dto.response.EmergencyReassignmentResponse;
import com.fonepay.gateway.entity.enums.Role;
import com.fonepay.gateway.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(ApiConstants.Emergency.BASE)
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyReassignmentService emergencyReassignmentService;
    private final CreateAppointmentService createAppointmentService;
    private final GetAllAppointmentsService getAllAppointmentsService;

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

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'PATIENT')")
    @PostMapping("/appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createEmergencyAppointment(
            @Valid @RequestBody AppointmentRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser.getRole() == Role.PATIENT) {
            request.setPatientId(currentUser.getId());
        }

        // If start/end time is not supplied, default to emergency window: now to now + 4 hours
        if (request.getScheduledStart() == null) {
            request.setScheduledStart(LocalDateTime.now().plusMinutes(5));
        }
        if (request.getScheduledEnd() == null) {
            request.setScheduledEnd(request.getScheduledStart().plusHours(4));
        }

        request.setIsEmergency(true);

        AppointmentResponse created = createAppointmentService.createAppointment(
                request,
                currentUser.getId(),
                currentUser.getRole().name()
        );

        ApiResponse<AppointmentResponse> response = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .message("Emergency dialysis appointment allocated successfully")
                .data(created)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getEmergencyAppointments() {
        List<AppointmentResponse> list = getAllAppointmentsService.getAppointments("EMERGENCY", null, null);

        ApiResponse<List<AppointmentResponse>> response = ApiResponse.<List<AppointmentResponse>>builder()
                .success(true)
                .message("Active emergency appointments retrieved successfully")
                .data(list)
                .build();

        return ResponseEntity.ok(response);
    }
}
