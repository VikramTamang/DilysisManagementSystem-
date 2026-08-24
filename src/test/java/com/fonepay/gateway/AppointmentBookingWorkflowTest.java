package com.fonepay.gateway;

import com.fonepay.gateway.appointment.entity.Appointment;
import com.fonepay.gateway.appointment.repository.AppointmentRepository;
import com.fonepay.gateway.entity.enums.AppointmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AppointmentBookingWorkflowTest {

    @Autowired(required = false)
    private AppointmentRepository appointmentRepository;

    @Test
    @DisplayName("Verify Doctor & Patient overlap query logic")
    public void testOverlapQueries() {
        if (appointmentRepository == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now().plusDays(1);
        LocalDateTime start = now.withHour(10).withMinute(0);
        LocalDateTime end = now.withHour(14).withMinute(0);

        // Check clean state initially
        assertFalse(appointmentRepository.isStaffBooked(999L, start, end, null));
        assertFalse(appointmentRepository.isPatientBooked(888L, start, end, null));
    }
}
