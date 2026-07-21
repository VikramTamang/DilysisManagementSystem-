package com.fonepay.gateway.dto.response;

import com.fonepay.gateway.entity.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long staffId;
    private String staffName;
    private Long roomId;
    private String roomNumber;
    private Long machineId;
    private String machineSerialNumber;
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private AppointmentStatus status;
}
