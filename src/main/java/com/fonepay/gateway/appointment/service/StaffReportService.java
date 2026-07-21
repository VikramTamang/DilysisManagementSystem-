package com.fonepay.gateway.appointment.service;

import com.fonepay.gateway.appointment.entity.StaffReport;
import com.fonepay.gateway.appointment.repository.StaffReportRepository;
import com.fonepay.gateway.user.entity.Doctor;
import com.fonepay.gateway.user.entity.Nurse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffReportService {

    private final StaffReportRepository staffReportRepository;

    @Transactional("appointmentTransactionManager")
    public void syncDoctorReport(Doctor doctor, String status) {
        log.info("Synchronizing Doctor ID {} into AppointmentDB staff_reports", doctor.getId());
        StaffReport report = StaffReport.builder()
                .staffId(doctor.getId())
                .name(doctor.getName())
                .email(doctor.getEmail())
                .phone(doctor.getPhone())
                .role("DOCTOR")
                .specializationOrQualification(doctor.getSpecialization())
                .shift("FULL_TIME")
                .status(status != null ? status : "ACTIVE")
                .build();

        staffReportRepository.save(report);
    }

    @Transactional("appointmentTransactionManager")
    public void syncNurseReport(Nurse nurse, String status) {
        log.info("Synchronizing Nurse ID {} into AppointmentDB staff_reports", nurse.getId());
        StaffReport report = StaffReport.builder()
                .staffId(nurse.getId())
                .name(nurse.getName())
                .email(nurse.getEmail())
                .phone(nurse.getPhone())
                .role("NURSE")
                .specializationOrQualification(nurse.getQualification())
                .shift(nurse.getShift() != null ? nurse.getShift() : "MORNING")
                .status(status != null ? status : "ACTIVE")
                .build();

        staffReportRepository.save(report);
    }

    @Transactional("appointmentTransactionManager")
    public void deleteStaffReport(Long staffId) {
        log.info("Removing Staff ID {} from AppointmentDB staff_reports", staffId);
        staffReportRepository.deleteById(staffId);
    }
}
