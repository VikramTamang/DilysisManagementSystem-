package com.fonepay.gateway.service.admin;

import com.fonepay.gateway.dto.response.AdminResponse;
import com.fonepay.gateway.factory.AdminFactory;
import com.fonepay.gateway.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllAdminsService {

    private final AdminRepository adminRepository;
    private final AdminFactory adminFactory;

    @Transactional(readOnly = true)
    public List<AdminResponse> getAllAdmins() {
        return adminRepository.findAll()
                .stream()
                .map(adminFactory::toResponse)
                .toList();
    }
}
