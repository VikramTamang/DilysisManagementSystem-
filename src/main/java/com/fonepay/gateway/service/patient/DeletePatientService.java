package com.fonepay.gateway.service.patient;

import com.fonepay.gateway.entity.Patient;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeletePatientService {

    private final PatientRepository patientRepository;

    @Transactional
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "Patient not found with id: " + id,
                        HttpStatus.NOT_FOUND,
                        "PATIENT_NOT_FOUND"
                ));
        patientRepository.delete(patient);
    }
}
