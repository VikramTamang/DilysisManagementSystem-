package com.fonepay.gateway.appointment.service.schedule;

import com.fonepay.gateway.appointment.entity.Schedule;
import com.fonepay.gateway.appointment.entity.StaffReport;
import com.fonepay.gateway.appointment.repository.ScheduleRepository;
import com.fonepay.gateway.appointment.repository.StaffReportRepository;
import com.fonepay.gateway.dto.response.ScheduleResponse;
import com.fonepay.gateway.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final StaffReportRepository staffReportRepository;

    public ScheduleResponse getScheduleById(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new AppException("Schedule not found", HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND"));

        StaffReport staff = staffReportRepository.findById(schedule.getStaffId()).orElse(null);
        String staffName = staff != null ? staff.getName() : "Unknown Staff";

        return CreateScheduleService.mapToResponse(schedule, staffName);
    }
}
