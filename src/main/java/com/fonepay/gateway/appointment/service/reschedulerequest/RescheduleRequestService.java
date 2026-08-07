package com.fonepay.gateway.appointment.service.reschedulerequest;

import com.fonepay.gateway.appointment.entity.Appointment;
import com.fonepay.gateway.appointment.entity.RescheduleRequest;
import com.fonepay.gateway.appointment.repository.AppointmentRepository;
import com.fonepay.gateway.appointment.repository.RescheduleRequestRepository;
import com.fonepay.gateway.appointment.service.appointment.RescheduleAppointmentService;
import com.fonepay.gateway.dto.request.RescheduleAppointmentRequest;
import com.fonepay.gateway.dto.request.RescheduleRequestCreateRequest;
import com.fonepay.gateway.dto.request.RescheduleRequestReviewRequest;
import com.fonepay.gateway.dto.response.RescheduleRequestResponse;
import com.fonepay.gateway.entity.enums.AppointmentStatus;
import com.fonepay.gateway.entity.enums.RescheduleRequestStatus;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.user.entity.PatientIdentity;
import com.fonepay.gateway.user.repository.PatientIdentityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RescheduleRequestService {

    private final RescheduleRequestRepository rescheduleRequestRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientIdentityRepository patientIdentityRepository;
    private final RescheduleAppointmentService rescheduleAppointmentService;

    @Transactional("appointmentTransactionManager")
    public RescheduleRequestResponse createRequest(RescheduleRequestCreateRequest request, Long patientId) {
        log.info("Patient {} requesting reschedule for appointment {}", patientId, request.getAppointmentId());

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new AppException("Appointment not found", HttpStatus.NOT_FOUND, "APPOINTMENT_NOT_FOUND"));

        if (!appointment.getPatientId().equals(patientId)) {
            throw new AppException("You can only request changes to your own appointments", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppException("Cannot request a reschedule for a cancelled appointment", HttpStatus.BAD_REQUEST, "APPOINTMENT_CANCELLED");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new AppException("Cannot request a reschedule for a completed appointment", HttpStatus.BAD_REQUEST, "APPOINTMENT_COMPLETED");
        }

        if (request.getRequestedStart().isAfter(request.getRequestedEnd()) || request.getRequestedStart().isEqual(request.getRequestedEnd())) {
            throw new AppException("Requested start time must be before end time", HttpStatus.BAD_REQUEST, "INVALID_SCHEDULE_TIME");
        }

        if (rescheduleRequestRepository.existsByAppointmentIdAndStatus(appointment.getId(), RescheduleRequestStatus.PENDING)) {
            throw new AppException("A pending reschedule request already exists for this appointment", HttpStatus.BAD_REQUEST, "REQUEST_ALREADY_PENDING");
        }

        RescheduleRequest rescheduleRequest = RescheduleRequest.builder()
                .appointmentId(appointment.getId())
                .patientId(patientId)
                .requestedStart(request.getRequestedStart())
                .requestedEnd(request.getRequestedEnd())
                .reason(request.getReason())
                .status(RescheduleRequestStatus.PENDING)
                .build();

        RescheduleRequest saved = rescheduleRequestRepository.save(rescheduleRequest);
        return mapToResponse(saved, appointment);
    }

    @Transactional("appointmentTransactionManager")
    public RescheduleRequestResponse approveRequest(Long id, RescheduleRequestReviewRequest request,
                                                    Long performedByUserId, String performedByRole) {
        log.info("Approving reschedule request ID: {}", id);

        RescheduleRequest rescheduleRequest = rescheduleRequestRepository.findById(id)
                .orElseThrow(() -> new AppException("Reschedule request not found", HttpStatus.NOT_FOUND, "RESCHEDULE_REQUEST_NOT_FOUND"));

        if (rescheduleRequest.getStatus() != RescheduleRequestStatus.PENDING) {
            throw new AppException("Only pending requests can be approved", HttpStatus.BAD_REQUEST, "REQUEST_NOT_PENDING");
        }

        // Delegates the actual reschedule to the existing service, so all the
        // same conflict checks (staff/room/machine availability) and audit
        // logging apply here exactly as they do for a staff-initiated reschedule.
        RescheduleAppointmentRequest appointmentReschedule = RescheduleAppointmentRequest.builder()
                .scheduledStart(rescheduleRequest.getRequestedStart())
                .scheduledEnd(rescheduleRequest.getRequestedEnd())
                .build();

        rescheduleAppointmentService.reschedule(
                rescheduleRequest.getAppointmentId(), appointmentReschedule, performedByUserId, performedByRole);

        rescheduleRequest.setStatus(RescheduleRequestStatus.APPROVED);
        rescheduleRequest.setReviewNote(request.getReviewNote());
        rescheduleRequest.setReviewedByUserId(performedByUserId);

        RescheduleRequest updated = rescheduleRequestRepository.save(rescheduleRequest);

        Appointment appointment = appointmentRepository.findById(updated.getAppointmentId()).orElse(null);
        return mapToResponse(updated, appointment);
    }

    @Transactional("appointmentTransactionManager")
    public RescheduleRequestResponse rejectRequest(Long id, RescheduleRequestReviewRequest request, Long performedByUserId) {
        log.info("Rejecting reschedule request ID: {}", id);

        RescheduleRequest rescheduleRequest = rescheduleRequestRepository.findById(id)
                .orElseThrow(() -> new AppException("Reschedule request not found", HttpStatus.NOT_FOUND, "RESCHEDULE_REQUEST_NOT_FOUND"));

        if (rescheduleRequest.getStatus() != RescheduleRequestStatus.PENDING) {
            throw new AppException("Only pending requests can be rejected", HttpStatus.BAD_REQUEST, "REQUEST_NOT_PENDING");
        }

        rescheduleRequest.setStatus(RescheduleRequestStatus.REJECTED);
        rescheduleRequest.setReviewNote(request.getReviewNote());
        rescheduleRequest.setReviewedByUserId(performedByUserId);

        RescheduleRequest updated = rescheduleRequestRepository.save(rescheduleRequest);

        Appointment appointment = appointmentRepository.findById(updated.getAppointmentId()).orElse(null);
        return mapToResponse(updated, appointment);
    }

    public RescheduleRequestResponse getRequestById(Long id, Long requestingUserId, boolean isPatientRole) {
        RescheduleRequest rescheduleRequest = rescheduleRequestRepository.findById(id)
                .orElseThrow(() -> new AppException("Reschedule request not found", HttpStatus.NOT_FOUND, "RESCHEDULE_REQUEST_NOT_FOUND"));

        if (isPatientRole && !rescheduleRequest.getPatientId().equals(requestingUserId)) {
            throw new AppException("Access denied: You can only view your own reschedule requests.", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }

        Appointment appointment = appointmentRepository.findById(rescheduleRequest.getAppointmentId()).orElse(null);
        return mapToResponse(rescheduleRequest, appointment);
    }

    public List<RescheduleRequestResponse> getRequestsForPatient(Long patientId) {
        return rescheduleRequestRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(this::mapWithAppointmentLookup)
                .collect(Collectors.toList());
    }

    public List<RescheduleRequestResponse> getAllRequests(RescheduleRequestStatus status) {
        List<RescheduleRequest> requests = status != null
                ? rescheduleRequestRepository.findByStatusOrderByCreatedAtDesc(status)
                : rescheduleRequestRepository.findAllByOrderByCreatedAtDesc();

        return requests.stream()
                .map(this::mapWithAppointmentLookup)
                .collect(Collectors.toList());
    }

    private RescheduleRequestResponse mapWithAppointmentLookup(RescheduleRequest rescheduleRequest) {
        Appointment appointment = appointmentRepository.findById(rescheduleRequest.getAppointmentId()).orElse(null);
        return mapToResponse(rescheduleRequest, appointment);
    }

    private RescheduleRequestResponse mapToResponse(RescheduleRequest rescheduleRequest, Appointment appointment) {
        String patientName = patientIdentityRepository.findById(rescheduleRequest.getPatientId())
                .map(PatientIdentity::getName)
                .orElse(null);

        return RescheduleRequestResponse.builder()
                .id(rescheduleRequest.getId())
                .appointmentId(rescheduleRequest.getAppointmentId())
                .patientId(rescheduleRequest.getPatientId())
                .patientName(patientName)
                .currentScheduledStart(appointment != null ? appointment.getScheduledStart() : null)
                .currentScheduledEnd(appointment != null ? appointment.getScheduledEnd() : null)
                .requestedStart(rescheduleRequest.getRequestedStart())
                .requestedEnd(rescheduleRequest.getRequestedEnd())
                .reason(rescheduleRequest.getReason())
                .status(rescheduleRequest.getStatus())
                .reviewNote(rescheduleRequest.getReviewNote())
                .createdAt(rescheduleRequest.getCreatedAt())
                .updatedAt(rescheduleRequest.getUpdatedAt())
                .build();
    }
}