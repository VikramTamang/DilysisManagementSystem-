package com.fonepay.gateway.service.admin;

import com.fonepay.gateway.dto.response.AdminResponse;
import com.fonepay.gateway.user.entity.Admin;
import com.fonepay.gateway.user.entity.User;
import com.fonepay.gateway.user.repository.AdminRepository;
import com.fonepay.gateway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAllAdminsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public List<AdminResponse> getAllAdmins() {
        return adminRepository.findAll().stream()
                .map(admin -> {
                    User user = userRepository.findById(admin.getId()).orElse(null);
                    return CreateAdminService.mapToResponse(user, admin);
                })
                .collect(Collectors.toList());
    }
}
