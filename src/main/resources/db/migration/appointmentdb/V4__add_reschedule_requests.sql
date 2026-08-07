CREATE TABLE reschedule_requests (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     appointment_id BIGINT NOT NULL,
                                     patient_id BIGINT NOT NULL,
                                     requested_start DATETIME NOT NULL,
                                     requested_end DATETIME NOT NULL,
                                     reason VARCHAR(500),
                                     status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                     review_note VARCHAR(500),
                                     reviewed_by_user_id BIGINT,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                     CONSTRAINT fk_reschedule_requests_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

CREATE INDEX idx_reschedule_requests_patient_id ON reschedule_requests(patient_id);
CREATE INDEX idx_reschedule_requests_status ON reschedule_requests(status);