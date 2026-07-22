package com.fonepay.gateway.appointment.service.report;

import com.fonepay.gateway.appointment.entity.Appointment;
import com.fonepay.gateway.appointment.entity.DialysisMachine;
import com.fonepay.gateway.appointment.entity.Room;
import com.fonepay.gateway.appointment.repository.AppointmentRepository;
import com.fonepay.gateway.appointment.repository.DialysisMachineRepository;
import com.fonepay.gateway.appointment.repository.RoomRepository;
import com.fonepay.gateway.dto.response.MachineUtilizationResponse;
import com.fonepay.gateway.dto.response.RoomUtilizationResponse;
import com.fonepay.gateway.dto.response.UtilizationReportResponse;
import com.fonepay.gateway.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilizationReportService {

    private final AppointmentRepository appointmentRepository;
    private final RoomRepository roomRepository;
    private final DialysisMachineRepository dialysisMachineRepository;

    public UtilizationReportResponse getUtilizationReport(LocalDateTime windowStart, LocalDateTime windowEnd) {
        if (windowStart.isAfter(windowEnd) || windowStart.isEqual(windowEnd)) {
            throw new AppException("windowStart must be before windowEnd", HttpStatus.BAD_REQUEST, "INVALID_REPORT_WINDOW");
        }

        long totalWindowMinutes = Duration.between(windowStart, windowEnd).toMinutes();

        List<Appointment> appointmentsInWindow = appointmentRepository.findAppointmentsInWindow(windowStart, windowEnd);

        // Clip each appointment's occupied time to the report window before summing,
        // so an appointment that starts before / ends after the window isn't over-counted.
        Map<Long, Long> bookedMinutesByRoom = appointmentsInWindow.stream()
                .collect(Collectors.groupingBy(Appointment::getRoomId,
                        Collectors.summingLong(a -> clippedMinutes(a, windowStart, windowEnd))));

        Map<Long, Long> appointmentCountByRoom = appointmentsInWindow.stream()
                .collect(Collectors.groupingBy(Appointment::getRoomId, Collectors.counting()));

        Map<Long, Long> bookedMinutesByMachine = appointmentsInWindow.stream()
                .collect(Collectors.groupingBy(Appointment::getMachineId,
                        Collectors.summingLong(a -> clippedMinutes(a, windowStart, windowEnd))));

        Map<Long, Long> appointmentCountByMachine = appointmentsInWindow.stream()
                .collect(Collectors.groupingBy(Appointment::getMachineId, Collectors.counting()));

        List<RoomUtilizationResponse> rooms = roomRepository.findAll().stream()
                .map(room -> mapRoom(room, bookedMinutesByRoom, appointmentCountByRoom, totalWindowMinutes))
                .collect(Collectors.toList());

        List<MachineUtilizationResponse> machines = dialysisMachineRepository.findAll().stream()
                .map(machine -> mapMachine(machine, bookedMinutesByMachine, appointmentCountByMachine, totalWindowMinutes))
                .collect(Collectors.toList());

        return UtilizationReportResponse.builder()
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .rooms(rooms)
                .machines(machines)
                .build();
    }

    private long clippedMinutes(Appointment appointment, LocalDateTime windowStart, LocalDateTime windowEnd) {
        LocalDateTime start = appointment.getScheduledStart().isBefore(windowStart) ? windowStart : appointment.getScheduledStart();
        LocalDateTime end = appointment.getScheduledEnd().isAfter(windowEnd) ? windowEnd : appointment.getScheduledEnd();
        return Duration.between(start, end).toMinutes();
    }

    private RoomUtilizationResponse mapRoom(Room room, Map<Long, Long> bookedMinutesByRoom,
                                            Map<Long, Long> appointmentCountByRoom, long totalWindowMinutes) {
        long bookedMinutes = bookedMinutesByRoom.getOrDefault(room.getId(), 0L);
        long totalAppointments = appointmentCountByRoom.getOrDefault(room.getId(), 0L);

        return RoomUtilizationResponse.builder()
                .roomId(room.getId())
                .roomNumber(room.getRoomNumber())
                .status(room.getStatus())
                .totalAppointments(totalAppointments)
                .bookedMinutes(bookedMinutes)
                .utilizationPercentage(percentage(bookedMinutes, totalWindowMinutes))
                .build();
    }

    private MachineUtilizationResponse mapMachine(DialysisMachine machine, Map<Long, Long> bookedMinutesByMachine,
                                                  Map<Long, Long> appointmentCountByMachine, long totalWindowMinutes) {
        long bookedMinutes = bookedMinutesByMachine.getOrDefault(machine.getId(), 0L);
        long totalAppointments = appointmentCountByMachine.getOrDefault(machine.getId(), 0L);

        return MachineUtilizationResponse.builder()
                .machineId(machine.getId())
                .serialNumber(machine.getSerialNumber())
                .status(machine.getStatus())
                .totalAppointments(totalAppointments)
                .bookedMinutes(bookedMinutes)
                .utilizationPercentage(percentage(bookedMinutes, totalWindowMinutes))
                .build();
    }

    private double percentage(long bookedMinutes, long totalWindowMinutes) {
        if (totalWindowMinutes <= 0) {
            return 0.0;
        }
        return Math.round((bookedMinutes * 10000.0 / totalWindowMinutes)) / 100.0;
    }
}