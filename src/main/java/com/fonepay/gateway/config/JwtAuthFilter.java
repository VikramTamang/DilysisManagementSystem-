package com.fonepay.gateway.config;

import com.fonepay.gateway.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.fonepay.gateway.config.CustomUserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // No Authorization header or doesn't start with "Bearer " — skip this filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the raw JWT token (strip "Bearer " prefix)
        final String jwt = authHeader.substring(7);
        String userEmail = null;

        try {
            userEmail = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            // Token is malformed or signature is invalid — log and let request continue
            // Spring Security will reject it at the authorization stage
            log.warn("JWT token extraction failed: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // Only proceed if we have an email AND no existing authentication for this request
        if (userEmail != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            if (jwtUtil.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                          // credentials not needed — token already proves identity
                                userDetails.getAuthorities()   // ROLE_MERCHANT or ROLE_ADMIN
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Tell Spring Security: this request is authenticated as this user
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("JWT authentication successful for user: {}", userEmail);
            } else {
                log.warn("JWT token is invalid or expired for user: {}", userEmail);
            }
        }

        // Always continue the filter chain — Spring Security handles authorization next
        filterChain.doFilter(request, response);
    }
}