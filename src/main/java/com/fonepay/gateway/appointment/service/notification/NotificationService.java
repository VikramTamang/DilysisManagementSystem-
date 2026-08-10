package com.fonepay.gateway.appointment.service.notification;

import com.fonepay.gateway.appointment.entity.Appointment;
import com.fonepay.gateway.appointment.entity.Notification;
import com.fonepay.gateway.appointment.repository.AppointmentRepository;
import com.fonepay.gateway.appointment.repository.NotificationRepository;
import com.fonepay.gateway.dto.request.DelayNotificationRequest;
import com.fonepay.gateway.dto.response.NotificationResponse;
import com.fonepay.gateway.entity.enums.NotificationType;
import com.fonepay.gateway.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a");

    private final NotificationRepository notificationRepository;
    private final AppointmentRepository appointmentRepository;

    // ---- Trigger methods, called from the appointment lifecycle services ----

    @Transactional("appointmentTransactionManager")
    public void notifyAppointmentConfirmed(Appointment appointment) {
        String message = String.format(
                "Your dialysis appointment is confirmed for %s.",
                appointment.getScheduledStart().format(DISPLAY_FORMAT));
        save(appointment.getPatientId(), appointment.getId(), NotificationType.APPOINTMENT_CONFIRMED, message);
    }

    @Transactional("appointmentTransactionManager")
    public void notifyAppointmentRescheduled(Appointment before, Appointment after) {
        String message = String.format(
                "Your appointment originally scheduled for %s has been moved to %s.",
                before.getScheduledStart().format(DISPLAY_FORMAT),
                after.getScheduledStart().format(DISPLAY_FORMAT));
        save(after.getPatientId(), after.getId(), NotificationType.APPOINTMENT_RESCHEDULED, message);
    }

    @Transactional("appointmentTransactionManager")
    public void notifyAppointmentCancelled(Appointment appointment) {
        String message = String.format(
                "Your appointment scheduled for %s has been cancelled.",
                appointment.getScheduledStart().format(DISPLAY_FORMAT));
        save(appointment.getPatientId(), appointment.getId(), NotificationType.APPOINTMENT_CANCELLED, message);
    }

    // ---- Manual delay notice, since there's no automatic "delayed" trigger ----

    @Transactional("appointmentTransactionManager")
    public NotificationResponse sendDelayNotice(DelayNotificationRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new AppException("Appointment not found", HttpStatus.NOT_FOUND, "APPOINTMENT_NOT_FOUND"));

        Notification notification = save(appointment.getPatientId(), appointment.getId(),
                NotificationType.APPOINTMENT_DELAYED, request.getMessage());

        return mapToResponse(notification);
    }

    // ---- Patient-facing read access ----

    public List<NotificationResponse> getNotificationsForPatient(Long patientId) {
        return notificationRepository.findByRecipientPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(Long patientId) {
        return notificationRepository.countByRecipientPatientIdAndIsReadFalse(patientId);
    }

    @Transactional("appointmentTransactionManager")
    public NotificationResponse markAsRead(Long id, Long patientId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new AppException("Notification not found", HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND"));

        if (!notification.getRecipientPatientId().equals(patientId)) {
            throw new AppException("Access denied: This notification does not belong to you.", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }

        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        return mapToResponse(updated);
    }

    @Transactional("appointmentTransactionManager")
    public void markAllAsRead(Long patientId) {
        List<Notification> unread = notificationRepository.findByRecipientPatientIdOrderByCreatedAtDesc(patientId).stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());

        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    private Notification save(Long patientId, Long appointmentId, NotificationType type, String message) {
        Notification notification = Notification.builder()
                .recipientPatientId(patientId)
                .appointmentId(appointmentId)
                .type(type)
                .message(message)
                .isRead(false)
                .build();

        return notificationRepository.save(notification);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .appointmentId(notification.getAppointmentId())
                .type(notification.getType())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}