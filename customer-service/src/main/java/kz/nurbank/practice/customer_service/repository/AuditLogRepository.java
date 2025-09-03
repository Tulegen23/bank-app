package kz.nurbank.practice.customer_service.repository;

import kz.nurbank.practice.customer_service.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}
