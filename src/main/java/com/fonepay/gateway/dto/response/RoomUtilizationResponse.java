package com.fonepay.gateway.dto.response;

import com.fonepay.gateway.entity.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomUtilizationResponse {
    private Long roomId;
    private String roomNumber;
    private RoomStatus status;
    private long totalAppointments;
    private long bookedMinutes;
    private double utilizationPercentage;
}