package com.fonepay.gateway.appointment.repository;

import com.fonepay.gateway.appointment.entity.RescheduleRequest;
import com.fonepay.gateway.entity.enums.RescheduleRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RescheduleRequestRepository extends JpaRepository<RescheduleRequest, Long> {

    List<RescheduleRequest> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<RescheduleRequest> findByStatusOrderByCreatedAtDesc(RescheduleRequestStatus status);

    List<RescheduleRequest> findAllByOrderByCreatedAtDesc();

    boolean existsByAppointmentIdAndStatus(Long appointmentId, RescheduleRequestStatus status);
}