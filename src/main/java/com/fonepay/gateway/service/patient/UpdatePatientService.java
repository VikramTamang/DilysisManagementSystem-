package com.fonepay.gateway.service.patient;

import com.fonepay.gateway.dto.request.PatientRequest;
import com.fonepay.gateway.dto.response.PatientResponse;
import com.fonepay.gateway.entity.Patient;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.factory.PatientFactory;
import com.fonepay.gateway.repository.PatientRepository;
import com.fonepay.gateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientFactory patientFactory;

    @Transactional
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "Patient not found with id: " + id,
                        HttpStatus.NOT_FOUND,
                        "PATIENT_NOT_FOUND"
                ));

        if (!patient.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(
                    "Email already in use",
                    HttpStatus.CONFLICT,
                    "PATIENT_EMAIL_EXISTS"
            );
        }

        patient.setName(request.getName());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setMedicalHistory(request.getMedicalHistory());

        return patientFactory.toResponse(patientRepository.save(patient));
    }
}
