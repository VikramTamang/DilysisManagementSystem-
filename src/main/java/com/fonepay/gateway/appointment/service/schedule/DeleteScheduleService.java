package com.fonepay.gateway.appointment.service.schedule;

import com.fonepay.gateway.appointment.entity.Schedule;
import com.fonepay.gateway.appointment.repository.ScheduleRepository;
import com.fonepay.gateway.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteScheduleService {

    private final ScheduleRepository scheduleRepository;

    @Transactional("appointmentTransactionManager")
    public void deleteSchedule(Long id) {
        log.info("Deleting schedule ID: {}", id);

        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new AppException("Schedule not found", HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND"));

        scheduleRepository.delete(schedule);
    }
}
