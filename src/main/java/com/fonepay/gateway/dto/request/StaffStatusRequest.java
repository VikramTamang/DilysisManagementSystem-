package com.fonepay.gateway.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffStatusRequest {

    @NotBlank(message = "Account status is required")
    @Pattern(regexp = "ACTIVE|SUSPENDED", message = "Account status must be ACTIVE or SUSPENDED")
    private String accountStatus;
}