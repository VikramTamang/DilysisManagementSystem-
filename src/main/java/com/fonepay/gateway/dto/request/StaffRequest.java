package com.fonepay.gateway.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StaffRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private LocalDate dateOfBirth;
    private String bloodGroup;

    private String shift;
    private String specialization;
    private String assignedRoom;
    private String designation;
    private LocalDate hireDate;
}
