package com.fonepay.gateway.controller;

import com.fonepay.gateway.appointment.service.notification.NotificationService;
import com.fonepay.gateway.constant.ApiConstants;
import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.DelayNotificationRequest;
import com.fonepay.gateway.dto.response.NotificationResponse;
import com.fonepay.gateway.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiConstants.Notification.BASE)
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'PATIENT')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal User currentUser) {

        List<NotificationResponse> list = (currentUser.getRole() == com.fonepay.gateway.entity.enums.Role.PATIENT)
                ? notificationService.getNotificationsForPatient(currentUser.getId())
                : List.of();

        ApiResponse<List<NotificationResponse>> response = ApiResponse.<List<NotificationResponse>>builder()
                .success(true)
                .message("Notifications retrieved successfully")
                .data(list)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'PATIENT')")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal User currentUser) {

        long count = (currentUser.getRole() == com.fonepay.gateway.entity.enums.Role.PATIENT)
                ? notificationService.getUnreadCount(currentUser.getId())
                : 0L;

        ApiResponse<Map<String, Long>> response = ApiResponse.<Map<String, Long>>builder()
                .success(true)
                .message("Unread count retrieved successfully")
                .data(Map.of("unreadCount", count))
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        NotificationResponse responseData = notificationService.markAsRead(id, currentUser.getId());

        ApiResponse<NotificationResponse> response = ApiResponse.<NotificationResponse>builder()
                .success(true)
                .message("Notification marked as read")
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'PATIENT')")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() == com.fonepay.gateway.entity.enums.Role.PATIENT) {
            notificationService.markAllAsRead(currentUser.getId());
        }

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("All notifications marked as read")
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @PostMapping("/delay")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendDelayNotice(
            @Valid @RequestBody DelayNotificationRequest request) {

        NotificationResponse responseData = notificationService.sendDelayNotice(request);

        ApiResponse<NotificationResponse> response = ApiResponse.<NotificationResponse>builder()
                .success(true)
                .message("Delay notice sent to patient")
                .data(responseData)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}