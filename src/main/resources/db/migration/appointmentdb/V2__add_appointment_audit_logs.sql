CREATE TABLE appointment_audit_logs (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        appointment_id BIGINT NOT NULL,
                                        action VARCHAR(50) NOT NULL,
                                        performed_by_user_id BIGINT,
                                        performed_by_role VARCHAR(50),
                                        old_status VARCHAR(50),
                                        new_status VARCHAR(50),
                                        old_scheduled_start DATETIME,
                                        new_scheduled_start DATETIME,
                                        old_scheduled_end DATETIME,
                                        new_scheduled_end DATETIME,
                                        old_room_id BIGINT,
                                        new_room_id BIGINT,
                                        old_machine_id BIGINT,
                                        new_machine_id BIGINT,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        INDEX idx_audit_appointment_id (appointment_id)
);