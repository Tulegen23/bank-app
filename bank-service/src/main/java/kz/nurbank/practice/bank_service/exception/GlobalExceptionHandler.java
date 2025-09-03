package kz.nurbank.practice.bank_service.exception;

import kz.nurbank.practice.bank_service.dto.ApiResponse;
import kz.nurbank.practice.bank_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final AuditLogService auditLogService;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String userId = getUserId();
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        auditLogService.logAction(userId, "валидация", "ошибка: " + errorMessage);
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Ошибка валидации: " + errorMessage, null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        String userId = getUserId();
        auditLogService.logAction(userId, "неправильный аргумент", "failed: " + ex.getMessage());
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Ошибка: " + ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception ex) {
        String userId = getUserId();
        auditLogService.logAction(userId, "Внутренняя ошибка", "ошибка: " + ex.getMessage());
        return ResponseEntity.internalServerError().body(new ApiResponse<>(500, "Внутренняя ошибка: " + ex.getMessage(), null));
    }

    private String getUserId() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "Неизвестный";
        }
    }
}