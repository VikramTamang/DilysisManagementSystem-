ALTER TABLE appointment_audit_logs 
ADD COLUMN old_staff_id BIGINT NULL,
ADD COLUMN new_staff_id BIGINT NULL;
