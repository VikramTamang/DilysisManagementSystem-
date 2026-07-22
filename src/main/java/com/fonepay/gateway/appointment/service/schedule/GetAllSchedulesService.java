package com.fonepay.gateway.appointment.service.schedule;

import com.fonepay.gateway.appointment.entity.Schedule;
import com.fonepay.gateway.appointment.entity.StaffReport;
import com.fonepay.gateway.appointment.repository.ScheduleRepository;
import com.fonepay.gateway.appointment.repository.StaffReportRepository;
import com.fonepay.gateway.dto.response.ScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAllSchedulesService {

    private final ScheduleRepository scheduleRepository;
    private final StaffReportRepository staffReportRepository;

    public List<ScheduleResponse> getAllSchedules() {
        return scheduleRepository.findAll()
                .stream()
                .map(this::mapSchedule)
                .collect(Collectors.toList());
    }

    public List<ScheduleResponse> getSchedulesByStaffId(Long staffId) {
        return scheduleRepository.findByStaffId(staffId)
                .stream()
                .map(this::mapSchedule)
                .collect(Collectors.toList());
    }

    private ScheduleResponse mapSchedule(Schedule schedule) {
        StaffReport staff = staffReportRepository.findById(schedule.getStaffId()).orElse(null);
        String staffName = staff != null ? staff.getName() : "Unknown Staff";
        return CreateScheduleService.mapToResponse(schedule, staffName);
    }
}
