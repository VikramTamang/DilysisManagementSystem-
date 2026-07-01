package com.fonepay.gateway.entity.enums;

public enum MerchantStatus {

    /**
     * Merchant registered but not yet verified/approved.
     * Cannot use the API yet.
     */
    PENDING,

    /**
     * Merchant is verified and fully operational.
     * All API endpoints accessible.
     */
    ACTIVE,

    /**
     * Temporarily blocked - e.g. suspicious activity, policy violation.
     * Login will be rejected by Spring Security via isAccountNonLocked().
     */
    SUSPENDED,

    /**
     * Account permanently closed/deactivated.
     * Login will be rejected by Spring Security via isEnabled().
     */
    INACTIVE
}