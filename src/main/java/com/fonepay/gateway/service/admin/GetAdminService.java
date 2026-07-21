package com.fonepay.gateway.service.admin;

import com.fonepay.gateway.dto.response.AdminResponse;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.user.entity.Admin;
import com.fonepay.gateway.user.entity.User;
import com.fonepay.gateway.user.repository.AdminRepository;
import com.fonepay.gateway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAdminService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public AdminResponse getAdminById(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new AppException("Admin not found", HttpStatus.NOT_FOUND, "ADMIN_NOT_FOUND"));

        User user = userRepository.findById(id).orElse(null);
        return CreateAdminService.mapToResponse(user, admin);
    }
}
