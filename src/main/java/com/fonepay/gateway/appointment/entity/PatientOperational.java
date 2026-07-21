package com.fonepay.gateway.appointment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientOperational {

    @Id
    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "assigned_doctor_id")
    private Long assignedDoctorId;

    @Column(name = "dialysis_history", columnDefinition = "TEXT")
    private String dialysisHistory;

    @Column(name = "treatment_notes", columnDefinition = "TEXT")
    private String treatmentNotes;

    @Column(name = "total_sessions")
    private Integer totalSessions;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
