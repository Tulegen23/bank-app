package kz.nurbank.practice.bank_service.service;

import kz.nurbank.practice.bank_service.model.AuditLog;
import kz.nurbank.practice.bank_service.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    private final AuditLogRepository auditLogRepository;

    //Выводить логи
    @Async
    public void logAction(String userId, String action, String result) {
        String effectiveUserId = userId != null ? userId : MDC.get("userId") != null ? MDC.get("userId") : "unknown";
        AuditLog auditLog = AuditLog.builder()
                .timestamp(ZonedDateTime.now())
                .userId(effectiveUserId)
                .action(action)
                .result(result)
                .build();
        auditLogRepository.save(auditLog);
        log.info("{} | {}", action, result);
    }
}
