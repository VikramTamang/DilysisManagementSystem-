package com.fonepay.gateway.appointment.repository;

import com.fonepay.gateway.appointment.entity.AppointmentAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentAuditLogRepository extends JpaRepository<AppointmentAuditLog, Long> {

    List<AppointmentAuditLog> findByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);

    List<AppointmentAuditLog> findAllByOrderByCreatedAtDesc();
}