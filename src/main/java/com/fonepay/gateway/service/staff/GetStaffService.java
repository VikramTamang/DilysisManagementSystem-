package com.fonepay.gateway.service.staff;

import com.fonepay.gateway.dto.response.StaffResponse;
import com.fonepay.gateway.entity.Staff;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.factory.StaffFactory;
import com.fonepay.gateway.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetStaffService {

    private final StaffRepository staffRepository;
    private final StaffFactory staffFactory;

    @Transactional(readOnly = true)
    public StaffResponse getStaffById(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "Staff not found with id: " + id,
                        HttpStatus.NOT_FOUND,
                        "STAFF_NOT_FOUND"
                ));
        return staffFactory.toResponse(staff);
    }
}
