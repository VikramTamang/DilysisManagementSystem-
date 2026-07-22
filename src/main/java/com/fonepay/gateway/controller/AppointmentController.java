package com.fonepay.gateway.controller;

import com.fonepay.gateway.appointment.service.appointment.CancelAppointmentService;
import com.fonepay.gateway.appointment.service.appointment.CreateAppointmentService;
import com.fonepay.gateway.appointment.service.appointment.GetAllAppointmentsService;
import com.fonepay.gateway.appointment.service.appointment.GetAppointmentService;
import com.fonepay.gateway.appointment.service.appointment.RescheduleAppointmentService;
import com.fonepay.gateway.appointment.service.report.GetAuditLogService;
import com.fonepay.gateway.appointment.service.availability.GetAvailabilityService;
import com.fonepay.gateway.appointment.service.appointment.UpdateAppointmentService;
import com.fonepay.gateway.constant.ApiConstants;
import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.AppointmentRequest;
import com.fonepay.gateway.dto.request.RescheduleAppointmentRequest;
import com.fonepay.gateway.dto.response.AppointmentResponse;
import com.fonepay.gateway.dto.response.AuditLogResponse;
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
    private final RescheduleAppointmentService rescheduleAppointmentService;
    private final GetAuditLogService getAuditLogService;

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody AppointmentRequest request,
            @AuthenticationPrincipal User currentUser) {

        AppointmentResponse created = createAppointmentService.createAppointment(
                request, currentUser.getId(), currentUser.getRole().name());

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
            @Valid @RequestBody AppointmentRequest request,
            @AuthenticationPrincipal User currentUser) {

        AppointmentResponse updated = updateAppointmentService.updateAppointment(
                id, request, currentUser.getId(), currentUser.getRole().name());

        ApiResponse<AppointmentResponse> response = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .message("Appointment updated successfully")
                .data(updated)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rescheduleAppointment(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleAppointmentRequest request,
            @AuthenticationPrincipal User currentUser) {

        AppointmentResponse rescheduled = rescheduleAppointmentService.reschedule(
                id, request, currentUser.getId(), currentUser.getRole().name());

        ApiResponse<AppointmentResponse> response = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .message("Appointment rescheduled successfully")
                .data(rescheduled)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAppointmentHistory(@PathVariable Long id) {
        List<AuditLogResponse> history = getAuditLogService.getHistoryForAppointment(id);

        ApiResponse<List<AuditLogResponse>> response = ApiResponse.<List<AuditLogResponse>>builder()
                .success(true)
                .message("Appointment history retrieved successfully")
                .data(history)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        cancelAppointmentService.cancelAppointment(id, currentUser.getId(), currentUser.getRole().name());

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