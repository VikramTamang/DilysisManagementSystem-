package com.fonepay.gateway.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    private String phone;

    private String email;

    @Column(name = "license_number")
    private String licenseNumber;

    private String specialization;

    @Column(name = "consultation_fee")
    private BigDecimal consultationFee;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
