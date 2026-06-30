package com.fonepay.gateway.repository;

import com.fonepay.gateway.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    /**
     * Useful for checking duplicates during create (email should be unique).
     */
    boolean existsByEmail(String email);

    /**
     * Useful later if we need to look up a merchant by email directly.
     */
    Optional<Merchant> findByEmail(String email);
}