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

    // --- DOCTOR ENDPOINTS ---
    public static final class Doctor {
        private Doctor() {}
        public static final String BASE = API_BASE + "/doctors";
    }

    // --- NURSE ENDPOINTS ---
    public static final class Nurse {
        private Nurse() {}
        public static final String BASE = API_BASE + "/nurses";
    }

    // --- ADMIN ENDPOINTS ---
    public static final class Admin {
        private Admin() {}
        public static final String BASE = API_BASE + "/admins";
    }

    // --- APPOINTMENT ENDPOINTS ---
    public static final class Appointment {
        private Appointment() {}
        public static final String BASE = API_BASE + "/appointments";
    }

    // --- AUTH ENDPOINTS ---
    public static final class Auth {
        private Auth() {}
        public static final String BASE = API_BASE + "/auth";
        public static final String LOGIN = BASE + "/login";
    }

    // --- SCHEDULE ENDPOINTS ---
    public static final class Schedule {
        private Schedule() {}
        public static final String BASE = API_BASE + "/schedules";
    }

    // --- REPORT ENDPOINTS ---
    public static final class Report {
        private Report() {}
        public static final String BASE = API_BASE + "/reports";
    }

    // --- EMERGENCY ENDPOINTS ---
    public static final class Emergency {
        private Emergency() {}
        public static final String BASE = API_BASE + "/emergency";
    }

    // --- ROOM ENDPOINTS ---
    public static final class Room {
        private Room() {}
        public static final String BASE = API_BASE + "/rooms";
    }
}