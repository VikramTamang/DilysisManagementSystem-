package com.fonepay.gateway.service.staff;

import com.fonepay.gateway.entity.Staff;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteStaffService {

    private final StaffRepository staffRepository;

    @Transactional
    public void deleteStaff(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "Staff not found with id: " + id,
                        HttpStatus.NOT_FOUND,
                        "STAFF_NOT_FOUND"
                ));
        staffRepository.delete(staff);
    }
}
