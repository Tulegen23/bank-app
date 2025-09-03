package kz.nurbank.practice.bank_service.repository;

import kz.nurbank.practice.bank_service.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}
