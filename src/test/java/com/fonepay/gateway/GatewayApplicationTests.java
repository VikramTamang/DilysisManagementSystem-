package com.fonepay.gateway;

import com.fonepay.gateway.dto.request.DoctorRequest;
import com.fonepay.gateway.dto.request.PatientRequest;
import com.fonepay.gateway.dto.response.DoctorResponse;
import com.fonepay.gateway.dto.response.PatientResponse;
import com.fonepay.gateway.service.patient.CreatePatientService;
import com.fonepay.gateway.user.repository.UserRepository;
import com.fonepay.gateway.user.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest
class GatewayApplicationTests {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private DoctorService doctorService;

	@Autowired
	private CreatePatientService createPatientService;

	@Test
	void testCreateDoctorAndPatient() {
		DoctorRequest docReq = DoctorRequest.builder()
				.name("Test Doctor")
				.email("test.doctor@hospital.com")
				.password("password123")
				.phone("9800000000")
				.licenseNumber("DOC-999")
				.specialization("Cardiology")
				.consultationFee(BigDecimal.valueOf(1000))
				.experienceYears(5)
				.build();

		DoctorResponse docResp = doctorService.createDoctor(docReq);
		System.out.println("DOCTOR_CREATED: " + docResp.getId());

		PatientRequest patReq = PatientRequest.builder()
				.name("Test Patient")
				.email("test.patient@example.com")
				.password("password123")
				.phone("9841000000")
				.address("Kathmandu")
				.dateOfBirth(LocalDate.of(1990, 1, 1))
				.bloodGroup("A+")
				.assignedDoctorId(docResp.getId())
				.dialysisHistory("None")
				.treatmentNotes("New patient")
				.build();

		PatientResponse patResp = createPatientService.createPatient(patReq);
		System.out.println("PATIENT_CREATED: " + patResp.getId());
	}
}
