package com.fonepay.gateway.appointment.repository;

import com.fonepay.gateway.appointment.entity.DialysisMachine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DialysisMachineRepository extends JpaRepository<DialysisMachine, Long> {

    boolean existsBySerialNumber(String serialNumber);

    @Query("SELECT m FROM DialysisMachine m WHERE m.status = 'AVAILABLE' AND NOT EXISTS (" +
            "  SELECT a FROM Appointment a WHERE a.machineId = m.id AND a.status <> 'CANCELLED' " +
            "  AND a.scheduledStart < :end AND a.scheduledEnd > :start" +
            ") ORDER BY m.id ASC")
    List<DialysisMachine> findAvailableMachines(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}