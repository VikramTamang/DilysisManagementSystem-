package com.fonepay.gateway.service;

import com.fonepay.gateway.dto.request.LoginRequest;
import com.fonepay.gateway.dto.request.MerchantRequest;
import com.fonepay.gateway.dto.request.RegisterRequest;
import com.fonepay.gateway.dto.response.AuthResponse;
import com.fonepay.gateway.dto.response.MerchantResponse;
import com.fonepay.gateway.entity.Merchant;
import com.fonepay.gateway.entity.enums.MerchantStatus;
import com.fonepay.gateway.entity.enums.Role;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.repository.MerchantRepository;
import com.fonepay.gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // --- Auth Methods ---

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.debug("Registering new merchant with email: {}", request.getEmail());

        if (merchantRepository.existsByEmail(request.getEmail())) {
            throw new AppException(
                    "Email already registered",
                    HttpStatus.CONFLICT,
                    "MERCHANT_EMAIL_EXISTS"
            );
        }

        Merchant merchant = Merchant.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .businessName(request.getBusinessName())
                .role(Role.MERCHANT)
                .status(MerchantStatus.ACTIVE)
                .build();

        merchant = merchantRepository.save(merchant);
        log.debug("Merchant registered successfully with id: {}", merchant.getId());

        String token = jwtUtil.generateToken(merchant);

        return AuthResponse.builder()
                .token(token)
                .merchantId(merchant.getId())
                .name(merchant.getName())
                .email(merchant.getEmail())
                .role(merchant.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.debug("Login attempt for email: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Merchant merchant = merchantRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(
                        "Merchant not found",
                        HttpStatus.NOT_FOUND,
                        "MERCHANT_NOT_FOUND"
                ));

        String token = jwtUtil.generateToken(merchant);
        log.debug("Login successful for merchant id: {}", merchant.getId());

        return AuthResponse.builder()
                .token(token)
                .merchantId(merchant.getId())
                .name(merchant.getName())
                .email(merchant.getEmail())
                .role(merchant.getRole().name())
                .build();
    }

    // --- CRUD Methods ---

    @Transactional
    public MerchantResponse createMerchant(MerchantRequest request) {
        if (merchantRepository.existsByEmail(request.getEmail())) {
            throw new AppException(
                    "Email already in use",
                    HttpStatus.CONFLICT,
                    "MERCHANT_EMAIL_EXISTS"
            );
        }

        Merchant merchant = Merchant.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .businessName(request.getBusinessName())
                .role(Role.MERCHANT)
                .status(MerchantStatus.ACTIVE)
                .build();

        merchant = merchantRepository.save(merchant);
        return toResponse(merchant);
    }

    @Transactional(readOnly = true)
    public MerchantResponse getMerchantById(Long id) {
        return toResponse(findMerchantOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<MerchantResponse> getAllMerchants() {
        return merchantRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MerchantResponse updateMerchant(Long id, MerchantRequest request) {
        Merchant merchant = findMerchantOrThrow(id);

        if (!merchant.getEmail().equals(request.getEmail())
                && merchantRepository.existsByEmail(request.getEmail())) {
            throw new AppException(
                    "Email already in use",
                    HttpStatus.CONFLICT,
                    "MERCHANT_EMAIL_EXISTS"
            );
        }

        merchant.setName(request.getName());
        merchant.setEmail(request.getEmail());
        merchant.setPhone(request.getPhone());
        merchant.setBusinessName(request.getBusinessName());

        return toResponse(merchantRepository.save(merchant));
    }

    @Transactional
    public void deleteMerchant(Long id) {
        merchantRepository.delete(findMerchantOrThrow(id));
    }

    // --- Private Helpers ---

    private Merchant findMerchantOrThrow(Long id) {
        return merchantRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "Merchant not found with id: " + id,
                        HttpStatus.NOT_FOUND,
                        "MERCHANT_NOT_FOUND"
                ));
    }

    private MerchantResponse toResponse(Merchant merchant) {
        return MerchantResponse.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .email(merchant.getEmail())
                .phone(merchant.getPhone())
                .businessName(merchant.getBusinessName())
                .createdAt(merchant.getCreatedAt())
                .updatedAt(merchant.getUpdatedAt())
                .build();
    }
}