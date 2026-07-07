package com.fonepay.gateway.service.admin;

import com.fonepay.gateway.entity.Admin;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteAdminService {

    private final AdminRepository adminRepository;

    @Transactional
    public void deleteAdmin(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "Admin not found with id: " + id,
                        HttpStatus.NOT_FOUND,
                        "ADMIN_NOT_FOUND"
                ));
        adminRepository.delete(admin);
    }
}
