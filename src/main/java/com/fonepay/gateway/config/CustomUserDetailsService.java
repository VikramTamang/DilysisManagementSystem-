package com.fonepay.gateway.config;

import com.fonepay.gateway.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MerchantRepository merchantRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return merchantRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Merchant not found with email: " + email
                ));
    }
}