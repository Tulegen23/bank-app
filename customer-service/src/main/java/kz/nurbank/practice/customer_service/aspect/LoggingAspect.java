package kz.nurbank.practice.customer_service.aspect;

import kz.nurbank.practice.customer_service.dto.ApiResponse;
import kz.nurbank.practice.customer_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final AuditLogService auditLogService;

    @AfterReturning(pointcut = "@annotation(logAction)", returning = "result")
    public void logSuccess(LogAction logAction, Object result) {
        String logMessage = extractLogMessage(result);
        auditLogService.logAction(logAction.action(), "успешно: " + logMessage);
    }

    @AfterThrowing(pointcut = "@annotation(logAction)", throwing = "ex")
    public void logFailure(LogAction logAction, Exception ex) {
        auditLogService.logAction(logAction.action(), "ошибка: " + ex.getMessage());
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
