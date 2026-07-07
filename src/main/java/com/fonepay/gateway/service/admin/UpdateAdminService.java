package com.fonepay.gateway.service.admin;

import com.fonepay.gateway.dto.request.AdminRequest;
import com.fonepay.gateway.dto.response.AdminResponse;
import com.fonepay.gateway.entity.Admin;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.factory.AdminFactory;
import com.fonepay.gateway.repository.AdminRepository;
import com.fonepay.gateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateAdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final AdminFactory adminFactory;

    @Transactional
    public AdminResponse updateAdmin(Long id, AdminRequest request) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "Admin not found with id: " + id,
                        HttpStatus.NOT_FOUND,
                        "ADMIN_NOT_FOUND"
                ));

        if (!admin.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(
                    "Email already in use",
                    HttpStatus.CONFLICT,
                    "ADMIN_EMAIL_EXISTS"
            );
        }

        admin.setName(request.getName());
        admin.setEmail(request.getEmail());
        admin.setPhone(request.getPhone());
        admin.setDateOfBirth(request.getDateOfBirth());
        admin.setBloodGroup(request.getBloodGroup());

        return adminFactory.toResponse(adminRepository.save(admin));
    }
}
