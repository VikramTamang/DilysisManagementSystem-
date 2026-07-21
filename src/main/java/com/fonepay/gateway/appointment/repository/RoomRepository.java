package com.fonepay.gateway.appointment.repository;

import com.fonepay.gateway.appointment.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE r.status = 'AVAILABLE' AND NOT EXISTS (" +
           "  SELECT a FROM Appointment a WHERE a.roomId = r.id AND a.status <> 'CANCELLED' " +
           "  AND a.scheduledStart < :end AND a.scheduledEnd > :start" +
           ") ORDER BY r.id ASC")
    List<Room> findAvailableRooms(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
