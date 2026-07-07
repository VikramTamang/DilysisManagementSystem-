package com.fonepay.gateway.service.admin;

import com.fonepay.gateway.dto.request.AdminRequest;
import com.fonepay.gateway.dto.response.AdminResponse;
import com.fonepay.gateway.entity.Admin;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.factory.AdminFactory;
import com.fonepay.gateway.repository.AdminRepository;
import com.fonepay.gateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateAdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final AdminFactory adminFactory;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public AdminResponse createAdmin(AdminRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(
                    "Email already in use",
                    HttpStatus.CONFLICT,
                    "ADMIN_EMAIL_EXISTS"
            );
        }

        request.setPassword(passwordEncoder.encode(request.getPassword()));
        Admin admin = adminFactory.toEntity(request);
        admin = adminRepository.save(admin);

        log.debug("Admin created with id: {}", admin.getId());
        return adminFactory.toResponse(admin);
    }
}
