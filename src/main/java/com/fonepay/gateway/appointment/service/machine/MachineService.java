package com.fonepay.gateway.appointment.service.machine;

import com.fonepay.gateway.appointment.entity.DialysisMachine;
import com.fonepay.gateway.appointment.repository.DialysisMachineRepository;
import com.fonepay.gateway.dto.request.MachineRequest;
import com.fonepay.gateway.dto.request.MachineStatusRequest;
import com.fonepay.gateway.dto.response.MachineResponse;
import com.fonepay.gateway.entity.enums.MachineStatus;
import com.fonepay.gateway.exception.AppException;
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
public class MachineService {

    private final DialysisMachineRepository dialysisMachineRepository;

    @Transactional("appointmentTransactionManager")
    public MachineResponse createMachine(MachineRequest request) {
        log.info("Creating dialysis machine: {}", request.getSerialNumber());

        if (dialysisMachineRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new AppException("Serial number already exists", HttpStatus.BAD_REQUEST, "MACHINE_ALREADY_EXISTS");
        }

        MachineStatus status = request.getStatus() != null ? request.getStatus() : MachineStatus.AVAILABLE;

        DialysisMachine machine = DialysisMachine.builder()
                .serialNumber(request.getSerialNumber())
                .status(status)
                .build();

        DialysisMachine saved = dialysisMachineRepository.save(machine);
        return mapToResponse(saved);
    }

    @Transactional("appointmentTransactionManager")
    public MachineResponse updateMachine(Long id, MachineRequest request) {
        log.info("Updating dialysis machine ID: {}", id);

        DialysisMachine machine = dialysisMachineRepository.findById(id)
                .orElseThrow(() -> new AppException("Machine not found", HttpStatus.NOT_FOUND, "MACHINE_NOT_FOUND"));

        if (!machine.getSerialNumber().equals(request.getSerialNumber()) &&
                dialysisMachineRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new AppException("Serial number already exists", HttpStatus.BAD_REQUEST, "MACHINE_ALREADY_EXISTS");
        }

        machine.setSerialNumber(request.getSerialNumber());
        if (request.getStatus() != null) {
            machine.setStatus(request.getStatus());
        }

        DialysisMachine updated = dialysisMachineRepository.save(machine);
        return mapToResponse(updated);
    }

    @Transactional("appointmentTransactionManager")
    public MachineResponse updateMachineStatus(Long id, MachineStatusRequest request) {
        log.info("Updating status for machine ID: {} to {}", id, request.getStatus());

        DialysisMachine machine = dialysisMachineRepository.findById(id)
                .orElseThrow(() -> new AppException("Machine not found", HttpStatus.NOT_FOUND, "MACHINE_NOT_FOUND"));

        machine.setStatus(request.getStatus());
        DialysisMachine updated = dialysisMachineRepository.save(machine);
        return mapToResponse(updated);
    }

    @Transactional("appointmentTransactionManager")
    public void deleteMachine(Long id) {
        log.info("Deleting machine ID: {}", id);

        DialysisMachine machine = dialysisMachineRepository.findById(id)
                .orElseThrow(() -> new AppException("Machine not found", HttpStatus.NOT_FOUND, "MACHINE_NOT_FOUND"));

        dialysisMachineRepository.delete(machine);
    }

    public MachineResponse getMachineById(Long id) {
        DialysisMachine machine = dialysisMachineRepository.findById(id)
                .orElseThrow(() -> new AppException("Machine not found", HttpStatus.NOT_FOUND, "MACHINE_NOT_FOUND"));
        return mapToResponse(machine);
    }

    public List<MachineResponse> getAllMachines() {
        return dialysisMachineRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private MachineResponse mapToResponse(DialysisMachine machine) {
        return MachineResponse.builder()
                .id(machine.getId())
                .serialNumber(machine.getSerialNumber())
                .status(machine.getStatus())
                .createdAt(machine.getCreatedAt())
                .updatedAt(machine.getUpdatedAt())
                .build();
    }
}