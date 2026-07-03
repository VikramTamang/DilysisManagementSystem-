package com.fonepay.gateway.factory;

import com.fonepay.gateway.dto.request.PatientRequest;
import com.fonepay.gateway.dto.response.PatientResponse;
import com.fonepay.gateway.entity.Patient;
import com.fonepay.gateway.entity.enums.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PatientFactory {

    public Patient toEntity(PatientRequest request) {
        log.debug("Building Patient entity from request");
        return Patient.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .bloodGroup(request.getBloodGroup())
                .medicalHistory(request.getMedicalHistory())
                .role(Role.PATIENT)
                .build();
    }

    public PatientResponse toResponse(Patient patient) {
        log.debug("Mapping Patient entity to response DTO");
        return PatientResponse.builder()
                .id(patient.getId())
                .name(patient.getName())
                .email(patient.getEmail())
                .phone(patient.getPhone())
                .dateOfBirth(patient.getDateOfBirth())
                .bloodGroup(patient.getBloodGroup())
                .medicalHistory(patient.getMedicalHistory())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }
}
