package com.fonepay.gateway.service;

import com.fonepay.gateway.dto.request.MerchantRequest;
import com.fonepay.gateway.dto.response.MerchantResponse;
import com.fonepay.gateway.entity.Merchant;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;

    @Transactional
    public MerchantResponse createMerchant(MerchantRequest request) {
        if (merchantRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email already in use", HttpStatus.CONFLICT, "MERCHANT_EMAIL_EXISTS");
        }

        Merchant merchant = Merchant.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .businessName(request.getBusinessName())
                .build();

        Merchant saved = merchantRepository.save(merchant);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MerchantResponse getMerchantById(Long id) {
        Merchant merchant = findMerchantOrThrow(id);
        return toResponse(merchant);
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

        // If the email is changing, make sure the new one isn't already taken by someone else
        if (!merchant.getEmail().equals(request.getEmail())
                && merchantRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email already in use", HttpStatus.CONFLICT, "MERCHANT_EMAIL_EXISTS");
        }

        merchant.setName(request.getName());
        merchant.setEmail(request.getEmail());
        merchant.setPhone(request.getPhone());
        merchant.setBusinessName(request.getBusinessName());

        Merchant updated = merchantRepository.save(merchant);
        return toResponse(updated);
    }

    @Transactional
    public void deleteMerchant(Long id) {
        Merchant merchant = findMerchantOrThrow(id);
        merchantRepository.delete(merchant);
    }

    // --- Private helpers ---

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