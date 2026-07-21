package com.fonepay.gateway.controller;

import com.fonepay.gateway.appointment.service.*;
import com.fonepay.gateway.constant.ApiConstants;
import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.AppointmentRequest;
import com.fonepay.gateway.dto.response.AppointmentResponse;
import com.fonepay.gateway.dto.response.AvailabilityResponse;
import com.fonepay.gateway.entity.enums.Role;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping(ApiConstants.Appointment.BASE)
@RequiredArgsConstructor
public class AppointmentController {

    private final CreateAppointmentService createAppointmentService;
    private final UpdateAppointmentService updateAppointmentService;
    private final CancelAppointmentService cancelAppointmentService;
    private final GetAppointmentService getAppointmentService;
    private final GetAllAppointmentsService getAllAppointmentsService;
    private final GetAvailabilityService getAvailabilityService;

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody AppointmentRequest request) {

        AppointmentResponse created = createAppointmentService.createAppointment(request);

        ApiResponse<AppointmentResponse> response = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .message("Appointment created successfully")
                .data(created)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest request) {

        AppointmentResponse updated = updateAppointmentService.updateAppointment(id, request);

        ApiResponse<AppointmentResponse> response = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .message("Appointment updated successfully")
                .data(updated)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelAppointment(@PathVariable Long id) {
        cancelAppointmentService.cancelAppointment(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Appointment cancelled successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        AppointmentResponse appointment = getAppointmentService.getAppointmentById(id);

        if (currentUser.getRole() == Role.PATIENT && !appointment.getPatientId().equals(currentUser.getId())) {
            throw new AppException("Access denied: You can only view your own appointments.", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }

        ApiResponse<AppointmentResponse> response = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .message("Appointment retrieved successfully")
                .data(appointment)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAllAppointments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) String date,
            @AuthenticationPrincipal User currentUser) {

        List<AppointmentResponse> list;
        if (currentUser.getRole() == Role.PATIENT) {
            list = getAllAppointmentsService.getAppointmentsByPatientId(currentUser.getId());
        } else {
            list = getAllAppointmentsService.getAppointments(status, staffId, date);
        }

        ApiResponse<List<AppointmentResponse>> response = ApiResponse.<List<AppointmentResponse>>builder()
                .success(true)
                .message("Appointments retrieved successfully")
                .data(list)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkAvailability(
            @RequestParam(required = false) String date,
            @RequestParam String start,
            @RequestParam String end) {

        LocalDateTime startTime;
        LocalDateTime endTime;

        try {
            if (date != null && !date.isBlank()) {
                LocalDate d = LocalDate.parse(date);
                LocalTime tStart = parseTime(start);
                LocalTime tEnd = parseTime(end);
                startTime = LocalDateTime.of(d, tStart);
                endTime = LocalDateTime.of(d, tEnd);
            } else {
                startTime = LocalDateTime.parse(start);
                endTime = LocalDateTime.parse(end);
            }
        } catch (Exception e) {
            throw new AppException("Invalid date/time format. Provide ISO LocalDateTime, or date=YYYY-MM-DD & start/end=HH:mm (e.g. start=10)", HttpStatus.BAD_REQUEST, "INVALID_DATE_TIME");
        }

        AvailabilityResponse availability = getAvailabilityService.checkAvailability(startTime, endTime);

        ApiResponse<AvailabilityResponse> response = ApiResponse.<AvailabilityResponse>builder()
                .success(true)
                .message("Availability retrieved successfully")
                .data(availability)
                .build();

        return ResponseEntity.ok(response);
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr.contains(":")) {
            return LocalTime.parse(timeStr);
        } else {
            int hour = Integer.parseInt(timeStr);
            return LocalTime.of(hour, 0);
        }
    }
}
