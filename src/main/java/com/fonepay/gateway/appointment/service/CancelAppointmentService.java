package com.fonepay.gateway.appointment.service;

import com.fonepay.gateway.appointment.entity.Appointment;
import com.fonepay.gateway.appointment.repository.AppointmentRepository;
import com.fonepay.gateway.entity.enums.AppointmentStatus;
import com.fonepay.gateway.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancelAppointmentService {

    private final AppointmentRepository appointmentRepository;

    @Transactional("appointmentTransactionManager")
    public void cancelAppointment(Long id) {
        log.info("Soft cancelling appointment ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException("Appointment not found", HttpStatus.NOT_FOUND, "APPOINTMENT_NOT_FOUND"));

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }
}
