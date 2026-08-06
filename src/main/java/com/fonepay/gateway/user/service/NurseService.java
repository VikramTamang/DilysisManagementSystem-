package com.fonepay.gateway.user.service;

import com.fonepay.gateway.appointment.service.report.StaffReportService;
import com.fonepay.gateway.dto.request.NurseRequest;
import com.fonepay.gateway.dto.response.NurseResponse;
import com.fonepay.gateway.entity.enums.Role;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.user.entity.Nurse;
import com.fonepay.gateway.user.entity.User;
import com.fonepay.gateway.user.repository.NurseRepository;
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
public class NurseService {

    private final UserRepository userRepository;
    private final NurseRepository nurseRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StaffReportService staffReportService;

    @Transactional("userTransactionManager")
    public NurseResponse createNurse(NurseRequest request) {
        log.info("Creating nurse account for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email already exists", HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_EXISTS");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.NURSE)
                .accountStatus("ACTIVE")
                .build();

        User savedUser = userRepository.save(user);

        Nurse nurse = Nurse.builder()
                .id(savedUser.getId())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .qualification(request.getQualification())
                .shift(request.getShift())
                .assignedDepartment(request.getAssignedDepartment())
                .experienceYears(request.getExperienceYears())
                .build();

        Nurse savedNurse = nurseRepository.save(nurse);

        // Sync to AppointmentDB staff_reports
        staffReportService.syncNurseReport(savedNurse, "ACTIVE");

        return mapToResponse(savedUser, savedNurse);
    }

    @Transactional("userTransactionManager")
    public NurseResponse updateNurse(Long id, NurseRequest request) {
        Nurse nurse = nurseRepository.findById(id)
                .orElseThrow(() -> new AppException("Nurse not found", HttpStatus.NOT_FOUND, "NURSE_NOT_FOUND"));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User account not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        user.setName(request.getName());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);

        nurse.setName(request.getName());
        nurse.setPhone(request.getPhone());
        nurse.setQualification(request.getQualification());
        nurse.setShift(request.getShift());
        nurse.setAssignedDepartment(request.getAssignedDepartment());
        nurse.setExperienceYears(request.getExperienceYears());

        Nurse updatedNurse = nurseRepository.save(nurse);

        // Sync to AppointmentDB staff_reports
        staffReportService.syncNurseReport(updatedNurse, user.getAccountStatus());

        return mapToResponse(user, updatedNurse);
    }

    @Transactional("userTransactionManager")
    public NurseResponse updateNurseStatus(Long id, String accountStatus) {
        Nurse nurse = nurseRepository.findById(id)
                .orElseThrow(() -> new AppException("Nurse not found", HttpStatus.NOT_FOUND, "NURSE_NOT_FOUND"));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User account not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        user.setAccountStatus(accountStatus);
        userRepository.save(user);

        // Keep AppointmentDB staff_reports in sync
        staffReportService.syncNurseReport(nurse, accountStatus);

        return mapToResponse(user, nurse);
    }

    @Transactional("userTransactionManager")
    public void deleteNurse(Long id) {
        Nurse nurse = nurseRepository.findById(id)
                .orElseThrow(() -> new AppException("Nurse not found", HttpStatus.NOT_FOUND, "NURSE_NOT_FOUND"));

        nurseRepository.delete(nurse);
        userRepository.deleteById(id);

        // Remove from AppointmentDB staff_reports
        staffReportService.deleteStaffReport(id);
    }

    public NurseResponse getNurseById(Long id) {
        Nurse nurse = nurseRepository.findById(id)
                .orElseThrow(() -> new AppException("Nurse not found", HttpStatus.NOT_FOUND, "NURSE_NOT_FOUND"));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User account not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        return mapToResponse(user, nurse);
    }

    public List<NurseResponse> getAllNurses() {
        return nurseRepository.findAll().stream()
                .map(nurse -> {
                    User user = userRepository.findById(nurse.getId()).orElse(null);
                    return mapToResponse(user, nurse);
                })
                .collect(Collectors.toList());
    }

    private NurseResponse mapToResponse(User user, Nurse nurse) {
        return NurseResponse.builder()
                .id(nurse.getId())
                .name(nurse.getName())
                .email(nurse.getEmail())
                .phone(nurse.getPhone())
                .qualification(nurse.getQualification())
                .shift(nurse.getShift())
                .assignedDepartment(nurse.getAssignedDepartment())
                .experienceYears(nurse.getExperienceYears())
                .accountStatus(user != null ? user.getAccountStatus() : "ACTIVE")
                .build();
    }
}