package com.fonepay.gateway.appointment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "staff_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffReport {

    @Id
    @Column(name = "staff_id")
    private Long staffId;

    @Column(nullable = false)
    private String name;

    private String email;

    private String phone;

    @Column(nullable = false)
    private String role; // DOCTOR or NURSE

    @Column(name = "specialization_or_qualification")
    private String specializationOrQualification;

    private String shift;

    private String status; // ACTIVE, INACTIVE

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
