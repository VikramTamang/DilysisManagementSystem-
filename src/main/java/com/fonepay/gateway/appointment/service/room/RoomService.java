package com.fonepay.gateway.appointment.service.room;

import com.fonepay.gateway.appointment.entity.Room;
import com.fonepay.gateway.appointment.repository.RoomRepository;
import com.fonepay.gateway.dto.request.RoomRequest;
import com.fonepay.gateway.dto.request.RoomStatusRequest;
import com.fonepay.gateway.dto.response.RoomResponse;
import com.fonepay.gateway.entity.enums.RoomStatus;
import com.fonepay.gateway.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;

    @Transactional("appointmentTransactionManager")
    public RoomResponse createRoom(RoomRequest request) {
        log.info("Creating room: {}", request.getRoomNumber());

        if (roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new AppException("Room number already exists", HttpStatus.BAD_REQUEST, "ROOM_ALREADY_EXISTS");
        }

        RoomStatus status = request.getStatus() != null ? request.getStatus() : RoomStatus.AVAILABLE;

        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .status(status)
                .build();

        Room saved = roomRepository.save(room);
        return mapToResponse(saved);
    }

    @Transactional("appointmentTransactionManager")
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        log.info("Updating room ID: {}", id);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException("Room not found", HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND"));

        // Check if new room number is already taken by another room
        if (!room.getRoomNumber().equals(request.getRoomNumber()) &&
                roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new AppException("Room number already exists", HttpStatus.BAD_REQUEST, "ROOM_ALREADY_EXISTS");
        }

        room.setRoomNumber(request.getRoomNumber());
        if (request.getStatus() != null) {
            room.setStatus(request.getStatus());
        }

        Room updated = roomRepository.save(room);
        return mapToResponse(updated);
    }

    @Transactional("appointmentTransactionManager")
    public RoomResponse updateRoomStatus(Long id, RoomStatusRequest request) {
        log.info("Updating status for room ID: {} to {}", id, request.getStatus());

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException("Room not found", HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND"));

        room.setStatus(request.getStatus());
        Room updated = roomRepository.save(room);
        return mapToResponse(updated);
    }

    @Transactional("appointmentTransactionManager")
    public void deleteRoom(Long id) {
        log.info("Deleting room ID: {}", id);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException("Room not found", HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND"));

        roomRepository.delete(room);
    }

    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException("Room not found", HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND"));
        return mapToResponse(room);
    }

    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RoomResponse mapToResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .status(room.getStatus())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
