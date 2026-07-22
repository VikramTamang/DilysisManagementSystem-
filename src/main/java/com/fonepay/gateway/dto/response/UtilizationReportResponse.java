package com.fonepay.gateway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilizationReportResponse {
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private List<RoomUtilizationResponse> rooms;
    private List<MachineUtilizationResponse> machines;
}