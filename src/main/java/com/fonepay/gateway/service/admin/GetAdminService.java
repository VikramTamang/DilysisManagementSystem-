package com.fonepay.gateway.service.admin;

import com.fonepay.gateway.dto.response.AdminResponse;
import com.fonepay.gateway.entity.Admin;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.factory.AdminFactory;
import com.fonepay.gateway.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAdminService {

    private final AdminRepository adminRepository;
    private final AdminFactory adminFactory;

    @Transactional(readOnly = true)
    public AdminResponse getAdminById(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "Admin not found with id: " + id,
                        HttpStatus.NOT_FOUND,
                        "ADMIN_NOT_FOUND"
                ));
        return adminFactory.toResponse(admin);
    }
}
