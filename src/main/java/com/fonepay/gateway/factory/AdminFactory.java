package com.fonepay.gateway.factory;

import com.fonepay.gateway.dto.request.AdminRequest;
import com.fonepay.gateway.dto.response.AdminResponse;
import com.fonepay.gateway.entity.Admin;
import com.fonepay.gateway.entity.enums.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdminFactory {

    public Admin toEntity(AdminRequest request) {
        log.debug("Building Admin entity from request");
        return Admin.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .bloodGroup(request.getBloodGroup())
                .role(Role.ADMIN)
                .build();
    }

    public AdminResponse toResponse(Admin admin) {
        log.debug("Mapping Admin entity to response DTO");
        return AdminResponse.builder()
                .id(admin.getId())
                .name(admin.getName())
                .email(admin.getEmail())
                .phone(admin.getPhone())
                .dateOfBirth(admin.getDateOfBirth())
                .bloodGroup(admin.getBloodGroup())
                .createdAt(admin.getCreatedAt())
                .updatedAt(admin.getUpdatedAt())
                .build();
    }
}
