package com.fonepay.gateway.user.service.admin;

import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.user.repository.AdminRepository;
import com.fonepay.gateway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteAdminService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    @Transactional("userTransactionManager")
    public void deleteAdmin(Long id) {
        log.info("Deleting admin ID: {}", id);

        if (!adminRepository.existsById(id)) {
            throw new AppException("Admin not found", HttpStatus.NOT_FOUND, "ADMIN_NOT_FOUND");
        }

        adminRepository.deleteById(id);
        userRepository.deleteById(id);
    }
}
