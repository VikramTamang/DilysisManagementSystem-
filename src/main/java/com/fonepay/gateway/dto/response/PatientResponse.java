package com.fonepay.gateway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private String bloodGroup;
    private Long assignedDoctorId;
    private String dialysisHistory;
    private String treatmentNotes;
    private Integer totalSessions;
    private String accountStatus;

    // Scheduling status information
    private String schedulingStatus; // SCHEDULED or UNSCHEDULED
    private Long activeAppointmentId;
    private String nextScheduledAppointment;
}
