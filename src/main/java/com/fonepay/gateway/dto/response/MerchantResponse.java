package com.fonepay.gateway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String businessName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}