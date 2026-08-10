package com.fonepay.gateway.appointment.repository;

import com.fonepay.gateway.appointment.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientPatientIdOrderByCreatedAtDesc(Long recipientPatientId);

    long countByRecipientPatientIdAndIsReadFalse(Long recipientPatientId);
}