package com.fonepay.gateway.dto.request;

import com.fonepay.gateway.entity.enums.MachineStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineStatusRequest {

    @NotNull(message = "Status is required")
    private MachineStatus status;
}