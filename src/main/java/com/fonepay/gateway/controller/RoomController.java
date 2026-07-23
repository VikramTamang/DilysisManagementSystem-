package com.fonepay.gateway.controller;

import com.fonepay.gateway.appointment.service.room.RoomService;
import com.fonepay.gateway.constant.ApiConstants;
import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.RoomRequest;
import com.fonepay.gateway.dto.request.RoomStatusRequest;
import com.fonepay.gateway.dto.response.RoomResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.Room.BASE)
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@Valid @RequestBody RoomRequest request) {
        RoomResponse responseData = roomService.createRoom(request);

        ApiResponse<RoomResponse> response = ApiResponse.<RoomResponse>builder()
                .success(true)
                .message("Room created successfully")
                .data(responseData)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms() {
        List<RoomResponse> list = roomService.getAllRooms();

        ApiResponse<List<RoomResponse>> response = ApiResponse.<List<RoomResponse>>builder()
                .success(true)
                .message("Rooms retrieved successfully")
                .data(list)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable Long id) {
        RoomResponse responseData = roomService.getRoomById(id);

        ApiResponse<RoomResponse> response = ApiResponse.<RoomResponse>builder()
                .success(true)
                .message("Room retrieved successfully")
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest request
    ) {
        RoomResponse responseData = roomService.updateRoom(id, request);

        ApiResponse<RoomResponse> response = ApiResponse.<RoomResponse>builder()
                .success(true)
                .message("Room updated successfully")
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoomStatus(
            @PathVariable Long id,
            @Valid @RequestBody RoomStatusRequest request
    ) {
        RoomResponse responseData = roomService.updateRoomStatus(id, request);

        ApiResponse<RoomResponse> response = ApiResponse.<RoomResponse>builder()
                .success(true)
                .message("Room status updated successfully")
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Room deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }
}
