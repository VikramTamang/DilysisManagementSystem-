package com.fonepay.gateway.appointment.repository;

import com.fonepay.gateway.appointment.entity.StaffReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffReportRepository extends JpaRepository<StaffReport, Long> {
    List<StaffReport> findByRole(String role);
}
