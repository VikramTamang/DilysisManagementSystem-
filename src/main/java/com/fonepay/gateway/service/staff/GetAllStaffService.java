package com.fonepay.gateway.service.staff;

import com.fonepay.gateway.dto.response.StaffResponse;
import com.fonepay.gateway.factory.StaffFactory;
import com.fonepay.gateway.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllStaffService {

    private final StaffRepository staffRepository;
    private final StaffFactory staffFactory;

    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaff() {
        return staffRepository.findAll()
                .stream()
                .map(staffFactory::toResponse)
                .toList();
    }
}
