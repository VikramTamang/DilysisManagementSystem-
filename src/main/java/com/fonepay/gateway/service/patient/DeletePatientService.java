package com.fonepay.gateway.service.patient;

import com.fonepay.gateway.appointment.repository.PatientOperationalRepository;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.user.repository.PatientIdentityRepository;
import com.fonepay.gateway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeletePatientService {

    private final UserRepository userRepository;
    private final PatientIdentityRepository patientIdentityRepository;
    private final PatientOperationalRepository patientOperationalRepository;

    @Transactional("userTransactionManager")
    public void deletePatient(Long id) {
        log.info("Deleting patient ID: {}", id);

        if (!patientIdentityRepository.existsById(id)) {
            throw new AppException("Patient not found", HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND");
        }

        patientOperationalRepository.deleteById(id);
        patientIdentityRepository.deleteById(id);
        userRepository.deleteById(id);
    }
}
