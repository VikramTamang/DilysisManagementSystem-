package com.fonepay.gateway.service.staff;

import com.fonepay.gateway.dto.request.StaffRequest;
import com.fonepay.gateway.dto.response.StaffResponse;
import com.fonepay.gateway.entity.Staff;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.factory.StaffFactory;
import com.fonepay.gateway.repository.StaffRepository;
import com.fonepay.gateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateStaffService {

    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final StaffFactory staffFactory;

    @Transactional
    public StaffResponse createStaff(StaffRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(
                    "Email already in use",
                    HttpStatus.CONFLICT,
                    "STAFF_EMAIL_EXISTS"
            );
        }

        Staff staff = staffFactory.toEntity(request);
        staff = staffRepository.save(staff);

        log.debug("Staff created with id: {}", staff.getId());
        return staffFactory.toResponse(staff);
    }
}
