package com.fonepay.gateway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NurseResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String qualification;
    private String shift;
    private String assignedDepartment;
    private Integer experienceYears;
    private String accountStatus;
}
