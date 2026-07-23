package com.fonepay.gateway.appointment.entity;

import com.fonepay.gateway.entity.enums.AppointmentStatus;
import com.fonepay.gateway.entity.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(name = "performed_by_user_id")
    private Long performedByUserId;

    @Column(name = "performed_by_role")
    private String performedByRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private AppointmentStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private AppointmentStatus newStatus;

    @Column(name = "old_scheduled_start")
    private LocalDateTime oldScheduledStart;

    @Column(name = "new_scheduled_start")
    private LocalDateTime newScheduledStart;

    @Column(name = "old_scheduled_end")
    private LocalDateTime oldScheduledEnd;

    @Column(name = "new_scheduled_end")
    private LocalDateTime newScheduledEnd;

    @Column(name = "old_room_id")
    private Long oldRoomId;

    @Column(name = "new_room_id")
    private Long newRoomId;

    @Column(name = "old_machine_id")
    private Long oldMachineId;

    @Column(name = "new_machine_id")
    private Long newMachineId;

    @Column(name = "old_staff_id")
    private Long oldStaffId;

    @Column(name = "new_staff_id")
    private Long newStaffId;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}