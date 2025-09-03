package kz.nurbank.practice.customer_service.controller;

import kz.nurbank.practice.customer_service.aspect.LogAction;
import kz.nurbank.practice.customer_service.dto.ApiResponse;
import kz.nurbank.practice.customer_service.dto.CustomerDTO;
import kz.nurbank.practice.customer_service.dto.RegistrationDTO;
import kz.nurbank.practice.customer_service.dto.UpdateCustomerDTO;
import kz.nurbank.practice.customer_service.mapper.CustomerMapper;
import kz.nurbank.practice.customer_service.model.Customer;
import kz.nurbank.practice.customer_service.model.CustomerActivity;
import kz.nurbank.practice.customer_service.repository.CustomerRepository;
import kz.nurbank.practice.customer_service.service.AuditLogService;
import kz.nurbank.practice.customer_service.service.CustomerService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer API", description = "Операции с клиентами: создание, поиск, проверка существования")
public class CustomerController {

    private final CustomerService service;
    private final CustomerRepository customerRepository;
    private final AuditLogService auditLogService;

    @Operation(summary = "Создать нового клиента", description = "Сохраняет клиента и пользователя, доступно только админам")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Клиент успешно создан", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Неверные данные", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка сервера", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @LogAction(action = "create customer")
    public ResponseEntity<ApiResponse<?>> createCustomer(@Valid @RequestBody RegistrationDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            String error = result.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            auditLogService.logAction("валидация", "ошибка: " + error);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Ошибка валидации: " + error));
        }
        try {
            Customer saved = service.save(dto);
            service.logCustomerActivity(saved.getId(), "CUSTOMER_CREATE", "Новый клиент создан по ИИН: " + dto.getIin());
            return ResponseEntity.ok(ApiResponse.success("Клиент успешно создан", saved.getId()));
        } catch (IllegalArgumentException e) {
            auditLogService.logAction("неправильный аргумент", "ошибка: " + e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Ошибка: " + e.getMessage()));
        } catch (Exception e) {
            auditLogService.logAction("общая ошибка", "ошибка: " + e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.error(500, "Внутренняя ошибка: " + e.getMessage()));
        }
    }

    @Operation(summary = "Получить клиента по ИИН", description = "Ищет клиента по ИИН, только для админов")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Клиент найден", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Клиент не найден", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Неверный формат ИИН", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка сервера", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{iin}")
    @LogAction(action = "get customer by IIN")
    public ResponseEntity<ApiResponse<CustomerDTO>> getByIin(@PathVariable String iin) {
        if (iin == null || !iin.matches("\\d{12}")) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "ИИН должен состоять из 12 цифр"));
        }

        try {
            return service.findByIin(iin)
                    .map(CustomerMapper::toDTO)
                    .map(dto -> ResponseEntity.ok(ApiResponse.success("Клиент найден", dto)))
                    .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error(404, "Клиент с ИИН " + iin + " не найден")));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(500, "Внутренняя ошибка: " + e.getMessage()));
        }
    }

    @Operation(summary = "Проверка существования клиента", description = "Проверяет, существует ли клиент по ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Проверка выполнена", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка сервера", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/exists/{id}")
    @LogAction(action = "check customer existence")
    public ResponseEntity<ApiResponse<Boolean>> existsById(@PathVariable Long id) {
        try {
            boolean exists = customerRepository.existsById(id);
            return ResponseEntity.ok(ApiResponse.success("Проверка выполнена", exists));
        } catch (Exception e) {
            auditLogService.logAction("общая ошибка", "ошибка: " + e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.error(500, "Внутренняя ошибка: " + e.getMessage()));
        }
    }

    @Operation(summary = "Обновить клиента по ID", description = "Ищет клиента по ID, и меняет его данные на новые")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Клиент успешно изменил данные", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Клиент не найден", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Неверные данные", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка сервера", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwnCustomer(#id)")
    @PutMapping("/{id}")
    @LogAction(action = "update customer")
    public ResponseEntity<ApiResponse<CustomerDTO>> updateCustomer(@PathVariable Long id, @Valid @RequestBody UpdateCustomerDTO updateCustomerDTO, BindingResult result) {
        if (result.hasErrors()) {
            String error = result.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            auditLogService.logAction("валидация", "ошибка: " + error);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Ошибка валидации: " + error));
        }

        try {
            return service.findById(id)
                    .map(customer -> {
                        customer.setFullName(updateCustomerDTO.getFullName());
                        customer.setPhone(updateCustomerDTO.getPhone());
                        Customer updated = customerRepository.save(customer);
                        return ResponseEntity.ok(ApiResponse.success("Клиент обновлен", CustomerMapper.toDTO(updated)));
                    })
                    .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error(404, "Клиент с ID " + id + " не найден")));
        } catch (IllegalArgumentException e) {
            auditLogService.logAction("неправильный аргумент", "ошибка: " + e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Ошибка: " + e.getMessage()));
        } catch (Exception e) {
            auditLogService.logAction("общая ошибка", "ошибка: " + e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.error(500, "Внутренняя ошибка: " + e.getMessage()));
        }
    }

    @Operation(summary = "Удалить клиента по ID", description = "Ищет клиента по ID, и удаляет его")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Клиент успешно удален", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Клиент не найден", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка сервера", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwnCustomer(#id)")
    @DeleteMapping("/{id}")
    @LogAction(action = "delete customer")
    public ResponseEntity<ApiResponse<Object>> deleteCustomer(@PathVariable Long id) {
        try {
            return service.findById(id)
                    .map(customer -> {
                        customerRepository.deleteById(id);
                        return ResponseEntity.ok(ApiResponse.success("Клиент удален", null));
                    })
                    .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error(404, "Клиент с ID " + id + " не найден")));
        } catch (Exception e) {
            auditLogService.logAction("общая ошибка", "ошибка: " + e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.error(500, "Внутренняя ошибка: " + e.getMessage()));
        }
    }

    @Operation(summary = "Получить историю клиента", description = "Клиент по ID возвращает историю клиента")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwnCustomer(#id)")
    @GetMapping("/{id}/activities")
    @LogAction(action = "get customer activities")
    public ResponseEntity<ApiResponse<List<CustomerActivity>>> getActivities(@PathVariable Long id) {
        List<CustomerActivity> activities = service.getCustomerActivities(id);
        if (activities.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "история не найдена"));
        }
        return ResponseEntity.ok(ApiResponse.success("история найдены", activities));
    }

    @Operation(summary = "Логирование историю клиента", description = "Связынные с клиентом историй сохраняеться тут")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwnCustomer(#id)")
    @PostMapping("/{id}/log-activity")
    @LogAction(action = "log account activity")
    public ResponseEntity<ApiResponse<?>> logAccountActivity(@PathVariable Long id, @RequestBody Map<String, String> activityData) {
        try {
            String actionType = activityData.get("тип действия");
            String details = activityData.get("детали");
            service.logCustomerActivity(id, actionType, details);
            return ResponseEntity.ok(ApiResponse.success("История сохранена", null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(500, "Внутренняя ошибка: " + e.getMessage()));
        }
    }
}