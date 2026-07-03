package com.fonepay.gateway.factory;

import com.fonepay.gateway.dto.request.StaffRequest;
import com.fonepay.gateway.dto.response.StaffResponse;
import com.fonepay.gateway.entity.Staff;
import com.fonepay.gateway.entity.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class StaffFactory {

    public Staff toEntity(StaffRequest request) {
        return Staff.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .bloodGroup(request.getBloodGroup())
                .shift(request.getShift())
                .specialization(request.getSpecialization())
                .assignedRoom(request.getAssignedRoom())
                .designation(request.getDesignation())
                .hireDate(request.getHireDate())
                .role(Role.STAFF)
                .build();
    }

    public StaffResponse toResponse(Staff staff) {
        return StaffResponse.builder()
                .id(staff.getId())
                .name(staff.getName())
                .email(staff.getEmail())
                .phone(staff.getPhone())
                .dateOfBirth(staff.getDateOfBirth())
                .bloodGroup(staff.getBloodGroup())
                .shift(staff.getShift())
                .specialization(staff.getSpecialization())
                .assignedRoom(staff.getAssignedRoom())
                .designation(staff.getDesignation())
                .hireDate(staff.getHireDate())
                .createdAt(staff.getCreatedAt())
                .updatedAt(staff.getUpdatedAt())
                .build();
    }
}
