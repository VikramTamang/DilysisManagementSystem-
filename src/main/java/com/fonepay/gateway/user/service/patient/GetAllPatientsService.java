package com.fonepay.gateway.user.service.patient;

import com.fonepay.gateway.appointment.entity.PatientOperational;
import com.fonepay.gateway.appointment.repository.PatientOperationalRepository;
import com.fonepay.gateway.dto.response.PatientResponse;
import com.fonepay.gateway.user.entity.PatientIdentity;
import com.fonepay.gateway.user.entity.User;
import com.fonepay.gateway.user.repository.PatientIdentityRepository;
import com.fonepay.gateway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAllPatientsService {

    private final UserRepository userRepository;
    private final PatientIdentityRepository patientIdentityRepository;
    private final PatientOperationalRepository patientOperationalRepository;

    public List<PatientResponse> getAllPatients() {
        return patientIdentityRepository.findAll().stream()
                .map(identity -> {
                    User user = userRepository.findById(identity.getId()).orElse(null);
                    PatientOperational operational = patientOperationalRepository.findById(identity.getId()).orElse(null);
                    return CreatePatientService.mapToResponse(user, identity, operational);
                })
                .collect(Collectors.toList());
    }
}
