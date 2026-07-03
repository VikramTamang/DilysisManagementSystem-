package com.fonepay.gateway.service.staff;

import com.fonepay.gateway.dto.request.StaffRequest;
import com.fonepay.gateway.dto.response.StaffResponse;
import com.fonepay.gateway.entity.Staff;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.factory.StaffFactory;
import com.fonepay.gateway.repository.StaffRepository;
import com.fonepay.gateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateStaffService {

    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final StaffFactory staffFactory;

    @Transactional
    public StaffResponse updateStaff(Long id, StaffRequest request) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "Staff not found with id: " + id,
                        HttpStatus.NOT_FOUND,
                        "STAFF_NOT_FOUND"
                ));

        if (!staff.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(
                    "Email already in use",
                    HttpStatus.CONFLICT,
                    "STAFF_EMAIL_EXISTS"
            );
        }

        staff.setName(request.getName());
        staff.setEmail(request.getEmail());
        staff.setPhone(request.getPhone());
        staff.setDateOfBirth(request.getDateOfBirth());
        staff.setBloodGroup(request.getBloodGroup());
        staff.setShift(request.getShift());
        staff.setSpecialization(request.getSpecialization());
        staff.setAssignedRoom(request.getAssignedRoom());
        staff.setDesignation(request.getDesignation());
        staff.setHireDate(request.getHireDate());

        return staffFactory.toResponse(staffRepository.save(staff));
    }
}
