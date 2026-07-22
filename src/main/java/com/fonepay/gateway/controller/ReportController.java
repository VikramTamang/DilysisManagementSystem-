package com.fonepay.gateway.controller;

import com.fonepay.gateway.appointment.service.report.GetAuditLogService;
import com.fonepay.gateway.appointment.service.report.StaffActivityReportService;
import com.fonepay.gateway.appointment.service.report.UtilizationReportService;
import com.fonepay.gateway.constant.ApiConstants;
import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.response.AuditLogResponse;
import com.fonepay.gateway.dto.response.StaffActivityResponse;
import com.fonepay.gateway.dto.response.UtilizationReportResponse;
import com.fonepay.gateway.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Admin-only reporting endpoints: room/machine utilization, staff activity,
 * and the raw scheduling-action audit log. All three are read-only views
 * over data already captured by the appointment services - nothing here
 * writes anything.
 */
@RestController
@RequestMapping(ApiConstants.Report.BASE)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final UtilizationReportService utilizationReportService;
    private final StaffActivityReportService staffActivityReportService;
    private final GetAuditLogService getAuditLogService;

    @GetMapping("/utilization")
    public ResponseEntity<ApiResponse<UtilizationReportResponse>> getUtilizationReport(
            @RequestParam String start,
            @RequestParam String end) {

        LocalDateTime windowStart = parseAsWindowStart(start);
        LocalDateTime windowEnd = parseAsWindowEnd(end);

        UtilizationReportResponse report = utilizationReportService.getUtilizationReport(windowStart, windowEnd);

        ApiResponse<UtilizationReportResponse> response = ApiResponse.<UtilizationReportResponse>builder()
                .success(true)
                .message("Utilization report generated successfully")
                .data(report)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/staff-activity")
    public ResponseEntity<ApiResponse<List<StaffActivityResponse>>> getStaffActivityReport(
            @RequestParam String start,
            @RequestParam String end) {

        LocalDateTime windowStart = parseAsWindowStart(start);
        LocalDateTime windowEnd = parseAsWindowEnd(end);

        List<StaffActivityResponse> report = staffActivityReportService.getStaffActivityReport(windowStart, windowEnd);

        ApiResponse<List<StaffActivityResponse>> response = ApiResponse.<List<StaffActivityResponse>>builder()
                .success(true)
                .message("Staff activity report generated successfully")
                .data(report)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAllAuditLogs() {
        List<AuditLogResponse> logs = getAuditLogService.getAllLogs();

        ApiResponse<List<AuditLogResponse>> response = ApiResponse.<List<AuditLogResponse>>builder()
                .success(true)
                .message("Scheduling action logs retrieved successfully")
                .data(logs)
                .build();

        return ResponseEntity.ok(response);
    }

    // Accepts either a plain date (YYYY-MM-DD, treated as start-of-day) or a full ISO LocalDateTime.
    private LocalDateTime parseAsWindowStart(String value) {
        try {
            if (value.length() <= 10) {
                return LocalDate.parse(value).atStartOfDay();
            }
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            throw new AppException("Invalid 'start'. Use YYYY-MM-DD or an ISO date-time.", HttpStatus.BAD_REQUEST, "INVALID_DATE_TIME");
        }
    }

    // Accepts either a plain date (YYYY-MM-DD, treated as end-of-day) or a full ISO LocalDateTime.
    private LocalDateTime parseAsWindowEnd(String value) {
        try {
            if (value.length() <= 10) {
                return LocalDate.parse(value).atTime(LocalTime.MAX);
            }
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            throw new AppException("Invalid 'end'. Use YYYY-MM-DD or an ISO date-time.", HttpStatus.BAD_REQUEST, "INVALID_DATE_TIME");
        }
    }
}