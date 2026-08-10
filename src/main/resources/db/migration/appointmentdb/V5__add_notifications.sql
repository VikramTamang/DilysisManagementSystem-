CREATE TABLE notifications (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               recipient_patient_id BIGINT NOT NULL,
                               appointment_id BIGINT,
                               type VARCHAR(50) NOT NULL,
                               message VARCHAR(500) NOT NULL,
                               is_read BOOLEAN NOT NULL DEFAULT FALSE,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_recipient ON notifications(recipient_patient_id);
CREATE INDEX idx_notifications_recipient_unread ON notifications(recipient_patient_id, is_read);