package com.fonepay.gateway.user.service.admin;

import com.fonepay.gateway.dto.request.AdminRequest;
import com.fonepay.gateway.dto.response.AdminResponse;
import com.fonepay.gateway.entity.enums.Role;
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
public class CreateAdminService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional("userTransactionManager")
    public AdminResponse createAdmin(AdminRequest request) {
        log.info("Creating admin account for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email already exists", HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_EXISTS");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .accountStatus("ACTIVE")
                .build();
        User savedUser = userRepository.save(user);

        Admin admin = Admin.builder()
                .id(savedUser.getId())
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .department(request.getDepartment())
                .build();
        Admin savedAdmin = adminRepository.save(admin);

        return mapToResponse(savedUser, savedAdmin);
    }

    public static AdminResponse mapToResponse(User user, Admin admin) {
        return AdminResponse.builder()
                .id(admin.getId())
                .name(admin.getName())
                .email(admin.getEmail())
                .phone(admin.getPhone())
                .department(admin.getDepartment())
                .accountStatus(user != null ? user.getAccountStatus() : "ACTIVE")
                .build();
    }
}
