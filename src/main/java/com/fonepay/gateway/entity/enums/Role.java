package com.fonepay.gateway.entity.enums;

public enum Role {

    /**
     * Standard merchant account.
     * Can manage their own transactions, view their own data.
     */
    MERCHANT,

    /**
     * Internal Fonepay admin account.
     * Elevated privileges - can view all merchants, manage platform settings.
     */
    ADMIN
}