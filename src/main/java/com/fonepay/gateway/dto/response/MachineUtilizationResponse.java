package com.fonepay.gateway.dto.response;

import com.fonepay.gateway.entity.enums.MachineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineUtilizationResponse {
    private Long machineId;
    private String serialNumber;
    private MachineStatus status;
    private long totalAppointments;
    private long bookedMinutes;
    private double utilizationPercentage;
}