package kz.nurbank.practice.customer_service.exception;

import kz.nurbank.practice.customer_service.dto.ApiResponse;
import kz.nurbank.practice.customer_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        auditLogService.logAction("валидация", "ошибка: " + errorMessage);
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "Ошибка валидации: " + errorMessage));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        auditLogService.logAction("неправильный аргумент", "ошибка: " + ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "Ошибка: " + ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception ex) {
        auditLogService.logAction("общая ошибка", "ошибка: " + ex.getMessage());
        return ResponseEntity.internalServerError().body(ApiResponse.error(500, "Внутренняя ошибка: " + ex.getMessage()));
    }
}