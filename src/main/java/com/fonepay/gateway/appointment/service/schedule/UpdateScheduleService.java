package com.fonepay.gateway.appointment.service.schedule;

import com.fonepay.gateway.appointment.entity.Schedule;
import com.fonepay.gateway.appointment.entity.StaffReport;
import com.fonepay.gateway.appointment.repository.ScheduleRepository;
import com.fonepay.gateway.appointment.repository.StaffReportRepository;
import com.fonepay.gateway.dto.request.ScheduleRequest;
import com.fonepay.gateway.dto.response.ScheduleResponse;
import com.fonepay.gateway.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final StaffReportRepository staffReportRepository;

    @Transactional("appointmentTransactionManager")
    public ScheduleResponse updateSchedule(Long id, ScheduleRequest request) {
        log.info("Updating schedule ID: {}", id);

        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new AppException("Schedule not found", HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND"));

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new AppException("Start time must be before end time", HttpStatus.BAD_REQUEST, "INVALID_SCHEDULE_TIME");
        }

        StaffReport staff = staffReportRepository.findById(request.getStaffId())
                .orElseThrow(() -> new AppException("Staff member not found", HttpStatus.NOT_FOUND, "STAFF_NOT_FOUND"));

        String dayOfWeek = request.getDayOfWeek().toUpperCase();

        List<Schedule> existing = scheduleRepository.findByStaffIdAndDayOfWeek(staff.getStaffId(), dayOfWeek);
        boolean overlaps = existing.stream()
                .filter(s -> !s.getId().equals(schedule.getId()))
                .anyMatch(s -> request.getStartTime().isBefore(s.getEndTime()) && request.getEndTime().isAfter(s.getStartTime()));

        if (overlaps) {
            throw new AppException("Schedule overlaps with an existing block for this staff member on " + dayOfWeek,
                    HttpStatus.BAD_REQUEST, "SCHEDULE_OVERLAP");
        }

        schedule.setStaffId(staff.getStaffId());
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : schedule.getIsAvailable());

        Schedule updated = scheduleRepository.save(schedule);

        return CreateScheduleService.mapToResponse(updated, staff.getName());
    }
}
