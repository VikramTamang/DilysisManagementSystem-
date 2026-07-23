package com.fonepay.gateway.appointment.repository;

import com.fonepay.gateway.appointment.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByStaffId(Long staffId);
    List<Schedule> findByStaffIdAndDayOfWeek(Long staffId, String dayOfWeek);
    List<Schedule> findByDayOfWeekAndIsAvailableTrue(String dayOfWeek);
}