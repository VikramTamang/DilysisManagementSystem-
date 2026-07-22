package com.fonepay.gateway.user.service.admin;

import com.fonepay.gateway.dto.request.AdminRequest;
import com.fonepay.gateway.dto.response.AdminResponse;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.user.entity.Admin;
import com.fonepay.gateway.user.entity.User;
import com.fonepay.gateway.user.repository.AdminRepository;
import com.fonepay.gateway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateAdminService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional("userTransactionManager")
    public AdminResponse updateAdmin(Long id, AdminRequest request) {
        log.info("Updating admin ID: {}", id);

        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new AppException("Admin not found", HttpStatus.NOT_FOUND, "ADMIN_NOT_FOUND"));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User account not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        user.setName(request.getName());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);

        admin.setName(request.getName());
        admin.setPhone(request.getPhone());
        admin.setDepartment(request.getDepartment());
        Admin updatedAdmin = adminRepository.save(admin);

        return CreateAdminService.mapToResponse(user, updatedAdmin);
    }
}
