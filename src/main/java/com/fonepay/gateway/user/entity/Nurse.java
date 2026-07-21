package com.fonepay.gateway.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "nurses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Nurse {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    private String phone;

    private String email;

    private String qualification;

    private String shift;

    @Column(name = "assigned_department")
    private String assignedDepartment;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
