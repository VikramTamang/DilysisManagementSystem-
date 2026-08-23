# Dialysis Management System

A hospital dialysis management system designed to streamline patient scheduling, manage medical staff workflows, prevent resource conflicts, and provide automated emergency staff reassignment.

---

## 🏗️ Project Architecture

This repository is organized as a monorepo containing both the backend service and the frontend web application:

```
DilysisManagementSystem/
├── pom.xml                 # Maven configuration for Spring Boot backend
├── mvnw / mvnw.cmd         # Maven wrapper
├── src/                    # Backend Source Code (Java 21 / Spring Boot 3)
│   ├── main/
│   │   ├── java/           # Controllers, Services, Repositories, Entities, Config
│   │   └── resources/      # application.yml & Flyway migrations (userdb, appointmentdb)
│   └── test/               # Integration tests
└── frontend/               # Frontend Application (Angular 21 / Tailwind CSS v4)
    ├── package.json
    ├── angular.json
    └── src/
        └── app/            # Standalone Components, Signals, Dashboards, Guards, Services
```

---

## 🚀 Getting Started

### Prerequisites
- **Java**: OpenJDK 21 or newer
- **Node.js**: v20.x or newer (npm v10+)
- **MySQL**: 8.0 or newer
- **Maven**: 3.9+ (or use `./mvnw`)

---

### 1. Backend Setup (Spring Boot 3)

1. **Configure MySQL Databases**:
   Ensure MySQL is running on port `3306` with the appropriate user/password in `src/main/resources/application.yml`.
   The application uses two databases:
   - `user_db` (Identity & User Profiles)
   - `appointment_db` (Appointments, Rooms, Machines, Schedules, Audit Logs)

2. **Run Migrations & Start Backend**:
   ```bash
   # Windows
   .\mvnw.cmd spring-boot:run

   # Linux / macOS
   ./mvnw spring-boot:run
   ```
   The backend API will start at: `http://localhost:8080`

---

### 2. Frontend Setup (Angular 21)

1. **Navigate to the frontend directory**:
   ```bash
   cd frontend
   ```

2. **Install Dependencies**:
   ```bash
   npm install
   ```

3. **Start the Development Server**:
   ```bash
   npm start
   ```
   Open your browser at: `http://localhost:4200`

---

## 🔑 Default Credentials

- **Admin Account**:
  - Email: `admin@hospital.com`
  - Password: `password123`
  - Role: `ADMIN`

---

## 🌟 Key Features

- **Multi-Database Separation**: Isolated user authentication and appointment domains.
- **Resource Conflict Prevention**: Real-time interval overlap checking for Doctors/Nurses, Dialysis Rooms, and Dialysis Machines.
- **Emergency Reassignment Engine**: Automated discovery and reassignment of available staff upon sudden unavailability.
- **Patient Self-Service**: Self-registration, appointment tracking, notifications, and rescheduling requests.
- **Role-Based Dashboards**: Dedicated UI views for Admin, Doctor, Nurse, and Patient.
- **Audit Logging & Analytics**: Track appointment lifecycle events, room/machine utilization rates, and staff activity.
