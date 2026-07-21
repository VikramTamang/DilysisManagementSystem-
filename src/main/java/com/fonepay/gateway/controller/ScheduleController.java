package com.fonepay.gateway.controller;

import com.fonepay.gateway.appointment.service.*;
import com.fonepay.gateway.constant.ApiConstants;
import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.ScheduleRequest;
import com.fonepay.gateway.dto.response.ScheduleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.Schedule.BASE)
@RequiredArgsConstructor
public class ScheduleController {

    private final CreateScheduleService createScheduleService;
    private final GetScheduleService getScheduleService;
    private final GetAllSchedulesService getAllSchedulesService;
    private final UpdateScheduleService updateScheduleService;
    private final DeleteScheduleService deleteScheduleService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(
            @Valid @RequestBody ScheduleRequest request) {

        ScheduleResponse created = createScheduleService.createSchedule(request);

        ApiResponse<ScheduleResponse> response = ApiResponse.<ScheduleResponse>builder()
                .success(true)
                .message("Schedule created successfully")
                .data(created)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getScheduleById(@PathVariable Long id) {
        ScheduleResponse schedule = getScheduleService.getScheduleById(id);

        ApiResponse<ScheduleResponse> response = ApiResponse.<ScheduleResponse>builder()
                .success(true)
                .message("Schedule retrieved successfully")
                .data(schedule)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getAllSchedules(
            @RequestParam(required = false) Long staffId) {

        List<ScheduleResponse> schedules = (staffId != null)
                ? getAllSchedulesService.getSchedulesByStaffId(staffId)
                : getAllSchedulesService.getAllSchedules();

        ApiResponse<List<ScheduleResponse>> response = ApiResponse.<List<ScheduleResponse>>builder()
                .success(true)
                .message("Schedules retrieved successfully")
                .data(schedules)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleRequest request) {

        ScheduleResponse updated = updateScheduleService.updateSchedule(id, request);

        ApiResponse<ScheduleResponse> response = ApiResponse.<ScheduleResponse>builder()
                .success(true)
                .message("Schedule updated successfully")
                .data(updated)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable Long id) {
        deleteScheduleService.deleteSchedule(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Schedule deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }
}