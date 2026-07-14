package com.fonepay.gateway.constant;

/**
 * Centralized configuration for all API endpoints.
 */
public final class ApiConstants {

    // Prevent instantiation
    private ApiConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Base API Path
    public static final String API_BASE = "/api/v1";

    // --- PATIENT ENDPOINTS ---
    public static final class Patient {
        private Patient() {}
        
        public static final String BASE = API_BASE + "/patients";
    }

    // --- STAFF ENDPOINTS ---
    public static final class Staff {
        private Staff() {}
        
        public static final String BASE = API_BASE + "/staff";
    }

    // --- ADMIN ENDPOINTS ---
    public static final class Admin {
        private Admin() {}

        public static final String BASE = API_BASE + "/admins";
    }

    // --- AUTH ENDPOINTS ---
    public static final class Auth {
        private Auth() {}
        
        public static final String BASE = API_BASE + "/auth";
        public static final String LOGIN = BASE + "/login";
        public static final String REFRESH_TOKEN = BASE + "/refresh-token";
    }
}
