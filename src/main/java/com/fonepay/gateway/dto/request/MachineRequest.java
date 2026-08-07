package com.fonepay.gateway.dto.request;

import com.fonepay.gateway.entity.enums.MachineStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineRequest {

    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    private MachineStatus status;
}