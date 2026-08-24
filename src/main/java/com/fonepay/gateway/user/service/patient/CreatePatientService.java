package com.fonepay.gateway.user.service.patient;

import com.fonepay.gateway.appointment.entity.PatientOperational;
import com.fonepay.gateway.appointment.repository.PatientOperationalRepository;
import com.fonepay.gateway.dto.request.PatientRegistrationRequest;
import com.fonepay.gateway.dto.request.PatientRequest;
import com.fonepay.gateway.dto.response.PatientResponse;
import com.fonepay.gateway.entity.enums.Role;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.user.entity.PatientIdentity;
import com.fonepay.gateway.user.entity.User;
import com.fonepay.gateway.user.repository.DoctorRepository;
import com.fonepay.gateway.user.repository.PatientIdentityRepository;
import com.fonepay.gateway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreatePatientService {

    private final UserRepository userRepository;
    private final PatientIdentityRepository patientIdentityRepository;
    private final PatientOperationalRepository patientOperationalRepository;
    private final DoctorRepository doctorRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional("userTransactionManager")
    public PatientResponse registerSelf(PatientRegistrationRequest request) {
        log.info("Self-registering new patient with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email already exists", HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_EXISTS");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.PATIENT)
                .accountStatus("ACTIVE")
                .build();
        User savedUser = userRepository.save(user);

        PatientIdentity identity = PatientIdentity.builder()
                .id(savedUser.getId())
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .bloodGroup(request.getBloodGroup())
                .build();
        PatientIdentity savedIdentity = patientIdentityRepository.save(identity);

        PatientOperational operational = PatientOperational.builder()
                .patientId(savedUser.getId())
                .totalSessions(0)
                .build();
        PatientOperational savedOperational = patientOperationalRepository.save(operational);

        return mapToResponse(savedUser, savedIdentity, savedOperational, "UNSCHEDULED", null, null);
    }

    @Transactional("userTransactionManager")
    public PatientResponse createPatient(PatientRequest request) {
        log.info("Admin creating new patient with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email already exists", HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_EXISTS");
        }

        if (request.getAssignedDoctorId() != null && !doctorRepository.existsById(request.getAssignedDoctorId())) {
            throw new AppException("Assigned doctor not found", HttpStatus.NOT_FOUND, "DOCTOR_NOT_FOUND");
        }

        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : "Patient@123";

        // 1. UserDB: Save central auth User
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.PATIENT)
                .accountStatus("ACTIVE")
                .build();
        User savedUser = userRepository.save(user);

        // 2. UserDB: Save Patient Identity profile
        PatientIdentity identity = PatientIdentity.builder()
                .id(savedUser.getId())
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .bloodGroup(request.getBloodGroup())
                .build();
        PatientIdentity savedIdentity = patientIdentityRepository.save(identity);

        // 3. AppointmentDB: Save Patient Operational record
        PatientOperational operational = PatientOperational.builder()
                .patientId(savedUser.getId())
                .assignedDoctorId(request.getAssignedDoctorId())
                .dialysisHistory(request.getDialysisHistory())
                .treatmentNotes(request.getTreatmentNotes())
                .totalSessions(0)
                .build();
        PatientOperational savedOperational = patientOperationalRepository.save(operational);

        return mapToResponse(savedUser, savedIdentity, savedOperational, "UNSCHEDULED", null, null);
    }

    public static PatientResponse mapToResponse(User user, PatientIdentity identity, PatientOperational operational) {
        return mapToResponse(user, identity, operational, "UNSCHEDULED", null, null);
    }

    public static PatientResponse mapToResponse(User user, PatientIdentity identity, PatientOperational operational,
                                                String schedulingStatus, Long activeAppointmentId, String nextScheduledAppointment) {
        return PatientResponse.builder()
                .id(identity.getId())
                .name(identity.getName())
                .email(user != null ? user.getEmail() : null)
                .phone(identity.getPhone())
                .address(identity.getAddress())
                .dateOfBirth(identity.getDateOfBirth())
                .bloodGroup(identity.getBloodGroup())
                .assignedDoctorId(operational != null ? operational.getAssignedDoctorId() : null)
                .dialysisHistory(operational != null ? operational.getDialysisHistory() : null)
                .treatmentNotes(operational != null ? operational.getTreatmentNotes() : null)
                .totalSessions(operational != null ? operational.getTotalSessions() : 0)
                .accountStatus(user != null ? user.getAccountStatus() : "ACTIVE")
                .schedulingStatus(schedulingStatus != null ? schedulingStatus : "UNSCHEDULED")
                .activeAppointmentId(activeAppointmentId)
                .nextScheduledAppointment(nextScheduledAppointment)
                .build();
    }
}
