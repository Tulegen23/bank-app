package kz.nurbank.practice.customer_service.service;

import kz.nurbank.practice.customer_service.model.AuditLog;
import kz.nurbank.practice.customer_service.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    private final AuditLogRepository auditLogRepository;

    //С помощью этого функции выводим логи
    @Async
    public void logAction(String action, String result) {
        AuditLog auditLog = AuditLog.builder()
                .timestamp(ZonedDateTime.now())
                .userId("User")
                .action(action)
                .result(result)
                .build();
        auditLogRepository.save(auditLog);
        log.info("{} | {} | {}", auditLog.getTimestamp(), action, result);
    }
}