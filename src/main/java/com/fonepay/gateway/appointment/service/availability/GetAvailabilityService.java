package com.fonepay.gateway.appointment.service.availability;

import com.fonepay.gateway.appointment.repository.AppointmentRepository;
import com.fonepay.gateway.appointment.repository.DialysisMachineRepository;
import com.fonepay.gateway.appointment.repository.RoomRepository;
import com.fonepay.gateway.appointment.repository.StaffReportRepository;
import com.fonepay.gateway.dto.response.AvailabilityResponse;
import com.fonepay.gateway.dto.response.AvailabilityResponse.MachineAvailability;
import com.fonepay.gateway.dto.response.AvailabilityResponse.RoomAvailability;
import com.fonepay.gateway.dto.response.AvailabilityResponse.StaffAvailability;
import com.fonepay.gateway.entity.enums.MachineStatus;
import com.fonepay.gateway.entity.enums.RoomStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAvailabilityService {

    private final RoomRepository roomRepository;
    private final DialysisMachineRepository dialysisMachineRepository;
    private final StaffReportRepository staffReportRepository;
    private final AppointmentRepository appointmentRepository;

    public AvailabilityResponse checkAvailability(LocalDateTime start, LocalDateTime end) {
        List<RoomAvailability> rooms = roomRepository.findAll().stream()
                .map(room -> RoomAvailability.builder()
                        .id(room.getId())
                        .roomNumber(room.getRoomNumber())
                        .available(room.getStatus() == RoomStatus.AVAILABLE &&
                                !appointmentRepository.isRoomBooked(room.getId(), start, end, null))
                        .build())
                .collect(Collectors.toList());

        List<MachineAvailability> machines = dialysisMachineRepository.findAll().stream()
                .map(machine -> MachineAvailability.builder()
                        .id(machine.getId())
                        .serialNumber(machine.getSerialNumber())
                        .available(machine.getStatus() == MachineStatus.AVAILABLE &&
                                !appointmentRepository.isMachineBooked(machine.getId(), start, end, null))
                        .build())
                .collect(Collectors.toList());

        List<StaffAvailability> staff = staffReportRepository.findAll().stream()
                .map(s -> StaffAvailability.builder()
                        .id(s.getStaffId())
                        .name(s.getName())
                        .email(s.getEmail())
                        .available(!appointmentRepository.isStaffBooked(s.getStaffId(), start, end, null))
                        .build())
                .collect(Collectors.toList());

        return AvailabilityResponse.builder()
                .rooms(rooms)
                .machines(machines)
                .staff(staff)
                .build();
    }
}
