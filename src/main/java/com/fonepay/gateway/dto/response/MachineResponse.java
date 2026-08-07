package com.fonepay.gateway.dto.response;

import com.fonepay.gateway.entity.enums.MachineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineResponse {
    private Long id;
    private String serialNumber;
    private MachineStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}