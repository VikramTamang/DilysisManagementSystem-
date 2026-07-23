package com.fonepay.gateway.dto.request;

import com.fonepay.gateway.entity.enums.RoomStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatusRequest {

    @NotNull(message = "Status is required")
    private RoomStatus status;
}
