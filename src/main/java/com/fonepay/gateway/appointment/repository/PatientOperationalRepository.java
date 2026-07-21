package com.fonepay.gateway.appointment.repository;

import com.fonepay.gateway.appointment.entity.PatientOperational;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientOperationalRepository extends JpaRepository<PatientOperational, Long> {
}
