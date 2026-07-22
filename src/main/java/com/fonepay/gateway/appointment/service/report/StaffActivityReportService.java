package com.fonepay.gateway.appointment.service.report;

import com.fonepay.gateway.appointment.entity.Appointment;
import com.fonepay.gateway.appointment.entity.StaffReport;
import com.fonepay.gateway.appointment.repository.AppointmentRepository;
import com.fonepay.gateway.appointment.repository.StaffReportRepository;
import com.fonepay.gateway.dto.response.StaffActivityResponse;
import com.fonepay.gateway.entity.enums.AppointmentStatus;
import com.fonepay.gateway.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffActivityReportService {

    private final AppointmentRepository appointmentRepository;
    private final StaffReportRepository staffReportRepository;

    public List<StaffActivityResponse> getStaffActivityReport(LocalDateTime windowStart, LocalDateTime windowEnd) {
        if (windowStart.isAfter(windowEnd) || windowStart.isEqual(windowEnd)) {
            throw new AppException("windowStart must be before windowEnd", HttpStatus.BAD_REQUEST, "INVALID_REPORT_WINDOW");
        }

        List<Appointment> appointmentsInWindow = appointmentRepository.findByScheduledStartBetween(windowStart, windowEnd);

        Map<Long, List<Appointment>> byStaff = appointmentsInWindow.stream()
                .collect(Collectors.groupingBy(Appointment::getStaffId));

        return staffReportRepository.findAll().stream()
                .map(staff -> mapStaff(staff, byStaff.getOrDefault(staff.getStaffId(), List.of())))
                .collect(Collectors.toList());
    }

    private StaffActivityResponse mapStaff(StaffReport staff, List<Appointment> appointments) {
        long scheduled = countByStatus(appointments, AppointmentStatus.SCHEDULED);
        long rescheduled = countByStatus(appointments, AppointmentStatus.RESCHEDULED);
        long cancelled = countByStatus(appointments, AppointmentStatus.CANCELLED);
        long completed = countByStatus(appointments, AppointmentStatus.COMPLETED);

        return StaffActivityResponse.builder()
                .staffId(staff.getStaffId())
                .name(staff.getName())
                .role(staff.getRole())
                .totalAppointments(appointments.size())
                .scheduledCount(scheduled)
                .rescheduledCount(rescheduled)
                .cancelledCount(cancelled)
                .completedCount(completed)
                .build();
    }

    private long countByStatus(List<Appointment> appointments, AppointmentStatus status) {
        return appointments.stream().filter(a -> a.getStatus() == status).count();
    }
}