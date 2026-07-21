package com.fonepay.gateway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {
    private List<RoomAvailability> rooms;
    private List<MachineAvailability> machines;
    private List<StaffAvailability> staff;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomAvailability {
        private Long id;
        private String roomNumber;
        private boolean available;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MachineAvailability {
        private Long id;
        private String serialNumber;
        private boolean available;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffAvailability {
        private Long id;
        private String name;
        private String email;
        private boolean available;
    }
}
