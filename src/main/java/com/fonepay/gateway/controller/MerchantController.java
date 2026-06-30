package com.fonepay.gateway.controller;

import com.fonepay.gateway.dto.ApiResponse;
import com.fonepay.gateway.dto.request.MerchantRequest;
import com.fonepay.gateway.dto.response.MerchantResponse;
import com.fonepay.gateway.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping
    public ResponseEntity<ApiResponse<MerchantResponse>> createMerchant(
            @Valid @RequestBody MerchantRequest request) {

        MerchantResponse created = merchantService.createMerchant(request);

        ApiResponse<MerchantResponse> response = ApiResponse.<MerchantResponse>builder()
                .success(true)
                .message("Merchant created successfully")
                .data(created)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MerchantResponse>> getMerchantById(@PathVariable Long id) {
        MerchantResponse merchant = merchantService.getMerchantById(id);

        ApiResponse<MerchantResponse> response = ApiResponse.<MerchantResponse>builder()
                .success(true)
                .message("Merchant retrieved successfully")
                .data(merchant)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MerchantResponse>>> getAllMerchants() {
        List<MerchantResponse> merchants = merchantService.getAllMerchants();

        ApiResponse<List<MerchantResponse>> response = ApiResponse.<List<MerchantResponse>>builder()
                .success(true)
                .message("Merchants retrieved successfully")
                .data(merchants)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MerchantResponse>> updateMerchant(
            @PathVariable Long id,
            @Valid @RequestBody MerchantRequest request) {

        MerchantResponse updated = merchantService.updateMerchant(id, request);

        ApiResponse<MerchantResponse> response = ApiResponse.<MerchantResponse>builder()
                .success(true)
                .message("Merchant updated successfully")
                .data(updated)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMerchant(@PathVariable Long id) {
        merchantService.deleteMerchant(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Merchant deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }
}