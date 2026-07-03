package com.fonepay.gateway.service.patient;

import com.fonepay.gateway.dto.response.PatientResponse;
import com.fonepay.gateway.factory.PatientFactory;
import com.fonepay.gateway.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllPatientsService {

    private final PatientRepository patientRepository;
    private final PatientFactory patientFactory;

    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(patientFactory::toResponse)
                .toList();
    }
}
