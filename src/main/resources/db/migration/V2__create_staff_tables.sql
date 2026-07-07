CREATE TABLE staff (
                       id BIGINT PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       phone VARCHAR(255) NOT NULL,
                       date_of_birth DATE,
                       blood_group VARCHAR(50),
                       role VARCHAR(50) NOT NULL,
                       shift VARCHAR(50),
                       specialization VARCHAR(255),
                       assigned_room VARCHAR(50),
                       designation VARCHAR(100),
                       hire_date DATE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);