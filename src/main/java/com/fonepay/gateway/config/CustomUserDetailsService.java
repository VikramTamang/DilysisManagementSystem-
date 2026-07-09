package com.fonepay.gateway.config;

import com.fonepay.gateway.entity.User;
import com.fonepay.gateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads a user (Patient, Staff, or Admin) by email for Spring Security.
 * UserRepository queries the abstract User type, which spans all three
 * TABLE_PER_CLASS tables, so this works regardless of which role the
 * email belongs to.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));

        return user;
    }
}