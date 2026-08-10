package com.fonepay.gateway.dto.response;

import com.fonepay.gateway.entity.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long appointmentId;
    private NotificationType type;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
}