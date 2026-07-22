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
public class CreateScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final StaffReportRepository staffReportRepository;

    @Transactional("appointmentTransactionManager")
    public ScheduleResponse createSchedule(ScheduleRequest request) {
        log.info("Creating schedule for staffId: {} on {}", request.getStaffId(), request.getDayOfWeek());

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new AppException("Start time must be before end time", HttpStatus.BAD_REQUEST, "INVALID_SCHEDULE_TIME");
        }

        StaffReport staff = staffReportRepository.findById(request.getStaffId())
                .orElseThrow(() -> new AppException("Staff member not found", HttpStatus.NOT_FOUND, "STAFF_NOT_FOUND"));

        String dayOfWeek = request.getDayOfWeek().toUpperCase();

        List<Schedule> existing = scheduleRepository.findByStaffIdAndDayOfWeek(staff.getStaffId(), dayOfWeek);
        boolean overlaps = existing.stream().anyMatch(s ->
                request.getStartTime().isBefore(s.getEndTime()) && request.getEndTime().isAfter(s.getStartTime()));

        if (overlaps) {
            throw new AppException("Schedule overlaps with an existing block for this staff member on " + dayOfWeek,
                    HttpStatus.BAD_REQUEST, "SCHEDULE_OVERLAP");
        }

        Schedule schedule = Schedule.builder()
                .staffId(staff.getStaffId())
                .dayOfWeek(dayOfWeek)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .build();

        Schedule saved = scheduleRepository.save(schedule);

        return mapToResponse(saved, staff.getName());
    }

    public static ScheduleResponse mapToResponse(Schedule schedule, String staffName) {
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .staffId(schedule.getStaffId())
                .staffName(staffName)
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .isAvailable(schedule.getIsAvailable())
                .build();
    }
}