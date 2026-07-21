package com.fonepay.gateway.user.service;

import com.fonepay.gateway.appointment.service.StaffReportService;
import com.fonepay.gateway.dto.request.DoctorRequest;
import com.fonepay.gateway.dto.response.DoctorResponse;
import com.fonepay.gateway.entity.enums.Role;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.user.entity.Doctor;
import com.fonepay.gateway.user.entity.User;
import com.fonepay.gateway.user.repository.DoctorRepository;
import com.fonepay.gateway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StaffReportService staffReportService;

    @Transactional("userTransactionManager")
    public DoctorResponse createDoctor(DoctorRequest request) {
        log.info("Creating doctor account for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email already exists", HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_EXISTS");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.DOCTOR)
                .accountStatus("ACTIVE")
                .build();

        User savedUser = userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .id(savedUser.getId())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .licenseNumber(request.getLicenseNumber())
                .specialization(request.getSpecialization())
                .consultationFee(request.getConsultationFee())
                .experienceYears(request.getExperienceYears())
                .build();

        Doctor savedDoctor = doctorRepository.save(doctor);

        // Sync to AppointmentDB staff_reports
        staffReportService.syncDoctorReport(savedDoctor, "ACTIVE");

        return mapToResponse(savedUser, savedDoctor);
    }

    @Transactional("userTransactionManager")
    public DoctorResponse updateDoctor(Long id, DoctorRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new AppException("Doctor not found", HttpStatus.NOT_FOUND, "DOCTOR_NOT_FOUND"));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User account not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        user.setName(request.getName());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);

        doctor.setName(request.getName());
        doctor.setPhone(request.getPhone());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setExperienceYears(request.getExperienceYears());

        Doctor updatedDoctor = doctorRepository.save(doctor);

        // Sync to AppointmentDB staff_reports
        staffReportService.syncDoctorReport(updatedDoctor, user.getAccountStatus());

        return mapToResponse(user, updatedDoctor);
    }

    @Transactional("userTransactionManager")
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new AppException("Doctor not found", HttpStatus.NOT_FOUND, "DOCTOR_NOT_FOUND"));

        doctorRepository.delete(doctor);
        userRepository.deleteById(id);

        // Remove from AppointmentDB staff_reports
        staffReportService.deleteStaffReport(id);
    }

    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new AppException("Doctor not found", HttpStatus.NOT_FOUND, "DOCTOR_NOT_FOUND"));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User account not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        return mapToResponse(user, doctor);
    }

    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(doctor -> {
                    User user = userRepository.findById(doctor.getId()).orElse(null);
                    return mapToResponse(user, doctor);
                })
                .collect(Collectors.toList());
    }

    private DoctorResponse mapToResponse(User user, Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .email(doctor.getEmail())
                .phone(doctor.getPhone())
                .licenseNumber(doctor.getLicenseNumber())
                .specialization(doctor.getSpecialization())
                .consultationFee(doctor.getConsultationFee())
                .experienceYears(doctor.getExperienceYears())
                .accountStatus(user != null ? user.getAccountStatus() : "ACTIVE")
                .build();
    }
}
