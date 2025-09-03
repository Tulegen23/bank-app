package kz.nurbank.practice.bank_service.aspect;

import kz.nurbank.practice.bank_service.dto.ApiResponse;
import kz.nurbank.practice.bank_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final AuditLogService auditLogService;

    @AfterReturning(pointcut = "@annotation(logAction)", returning = "result")
    public void logActionResult(LogAction logAction, Object result) {
        String userId = getUserId();
        String logMessage = extractLogMessage(result);

        if (result instanceof ResponseEntity) {
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            if (response.getStatusCode().is2xxSuccessful()) {
                auditLogService.logAction(userId, logAction.action(), "успешно: " + logMessage);
            } else {
                auditLogService.logAction(userId, logAction.action(), "ошибка: " + logMessage);
            }
        } else {
            auditLogService.logAction(userId, logAction.action(), "успешно: " + logMessage);
        }
    }

    @AfterThrowing(pointcut = "@annotation(logAction)", throwing = "ex")
    public void logFailure(LogAction logAction, Exception ex) {
        String userId = getUserId();
        auditLogService.logAction(userId, logAction.action(), "ошибка: " + ex.getMessage());
    }

    private String getUserId() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "Неизвестный";
        }
    }

    private String extractLogMessage(Object result) {
        if (result instanceof ResponseEntity) {
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            Object body = response.getBody();
            if (body instanceof ApiResponse) {
                ApiResponse<?> apiResponse = (ApiResponse<?>) body;
                return apiResponse.getMessage();
            }
            return body != null ? body.toString() : "нет тела";
        }
        return result != null ? result.toString() : "нет результата";
    }
}