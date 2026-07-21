package com.fonepay.gateway.appointment.repository;

import com.fonepay.gateway.appointment.entity.Appointment;
import com.fonepay.gateway.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.status <> 'CANCELLED' " +
           "AND a.scheduledStart < :end AND a.scheduledEnd > :start " +
           "AND a.staffId = :staffId " +
           "AND (:excludeId IS NULL OR a.id <> :excludeId)")
    boolean isStaffBooked(@Param("staffId") Long staffId,
                          @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end,
                          @Param("excludeId") Long excludeId);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.status <> 'CANCELLED' " +
           "AND a.scheduledStart < :end AND a.scheduledEnd > :start " +
           "AND a.roomId = :roomId " +
           "AND (:excludeId IS NULL OR a.id <> :excludeId)")
    boolean isRoomBooked(@Param("roomId") Long roomId,
                         @Param("start") LocalDateTime start,
                         @Param("end") LocalDateTime end,
                         @Param("excludeId") Long excludeId);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.status <> 'CANCELLED' " +
           "AND a.scheduledStart < :end AND a.scheduledEnd > :start " +
           "AND a.machineId = :machineId " +
           "AND (:excludeId IS NULL OR a.id <> :excludeId)")
    boolean isMachineBooked(@Param("machineId") Long machineId,
                            @Param("start") LocalDateTime start,
                            @Param("end") LocalDateTime end,
                            @Param("excludeId") Long excludeId);

    @Query("SELECT a FROM Appointment a WHERE " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:staffId IS NULL OR a.staffId = :staffId) AND " +
           "(:startOfDay IS NULL OR a.scheduledStart >= :startOfDay) AND " +
           "(:endOfDay IS NULL OR a.scheduledStart <= :endOfDay)")
    List<Appointment> findFilteredAppointments(@Param("status") AppointmentStatus status,
                                               @Param("staffId") Long staffId,
                                               @Param("startOfDay") LocalDateTime startOfDay,
                                               @Param("endOfDay") LocalDateTime endOfDay);

    List<Appointment> findByPatientId(Long patientId);
}
