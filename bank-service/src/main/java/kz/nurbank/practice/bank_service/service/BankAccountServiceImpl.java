package kz.nurbank.practice.bank_service.service;

import io.github.resilience4j.retry.Retry;
import kz.nurbank.practice.bank_service.config.CustomerServiceConfig;
import kz.nurbank.practice.bank_service.dto.ApiResponse;
import kz.nurbank.practice.bank_service.dto.BalanceDTO;
import kz.nurbank.practice.bank_service.dto.BankAccountDTO;
import kz.nurbank.practice.bank_service.mapper.BankAccountMapper;
import kz.nurbank.practice.bank_service.model.BankAccount;
import kz.nurbank.practice.bank_service.model.Currency;
import kz.nurbank.practice.bank_service.repository.BankAccountRepository;
import kz.nurbank.practice.bank_service.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {
    private static final Logger log = LoggerFactory.getLogger(BankAccountServiceImpl.class);
    private final BankAccountRepository repository;
    private final RestTemplate restTemplate;
    private final CustomerServiceConfig customerServiceConfig;
    private final Retry retry;
    private final AuditLogService auditLogService;
    private final JwtUtils jwtUtils;

    public String createAccount(Long clientId, Currency currency) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String checkUrl = customerServiceConfig.getUrl() + clientId;

        Boolean exists = Retry.decorateSupplier(retry, () -> {
            try {
                String token = jwtUtils.generateToken(userId);
                log.debug("Отправка токена для createAccount: {}", token);
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + token);
                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<ApiResponse<Boolean>> responseEntity = restTemplate.exchange(
                        checkUrl, HttpMethod.GET, entity, new ParameterizedTypeReference<ApiResponse<Boolean>>() {}
                );
                ApiResponse<Boolean> response = responseEntity.getBody();
                return response != null && response.getData() != null && response.getData();
            } catch (HttpServerErrorException e) {
                log.error("Ошибка при проверке клиента {}: {}", clientId, e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("Неожиданная ошибка при проверке клиента {}: {}", clientId, e.getMessage());
                throw new RuntimeException("Ошибка при обращении к customer_service", e);
            }
        }).get();

        if (!exists) {
            auditLogService.logAction(userId, "создание аккаунта", "ошибка: клиент с " + clientId + " не найден");
            throw new IllegalArgumentException("Клиент не найден");
        }

        String accountNumber = generateNumericAccountNumber();

        BankAccount account = BankAccount.builder()
                .accountNumber(accountNumber)
                .clientId(clientId)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .build();

        repository.save(account);
        auditLogService.logAction(userId, "создание аккаунта", "успешно: создание аккаунта " + accountNumber + " клиент с " + clientId);

        String activityUrl = customerServiceConfig.getUrl().replace("/exists/", "/") + clientId + "/log-activity";
        try {
            String token = jwtUtils.generateToken(userId);
            log.debug("Отправка токена для регистрации активности: {}", token);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> activityMap = Map.of("тип действия", "Создание аккаунта", "детали", "Аккаунт: " + accountNumber + ", Валюта: " + currency);
            HttpEntity<Map<String, String>> postEntity = new HttpEntity<>(activityMap, headers);

            ResponseEntity<ApiResponse> postResponse = restTemplate.exchange(
                    activityUrl, HttpMethod.POST, postEntity, ApiResponse.class
            );
        } catch (Exception e) {
            log.error("Логирование в Customer Service неправильно: {}", e.getMessage());
            auditLogService.logAction(userId, "регистрировать активность аккаунта", "ошибка: " + e.getMessage());
        }

        return accountNumber;
    }

    public List<BankAccountDTO> getAccountsByClientId(Long clientId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<BankAccountDTO> accounts = repository.findByClientId(clientId).stream()
                .map(BankAccountMapper::toDTO)
                .collect(Collectors.toList());
        auditLogService.logAction(userId, "получить аккаунты", "успешно: найден " + accounts.size() + " аккаунты клиента " + clientId);
        return accounts;
    }

    private String generateNumericAccountNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("KZ-");
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private static final BigDecimal DEPOSIT_FEE = new BigDecimal("3");
    private static final BigDecimal MAX_DEPOSIT_LIMIT_KZT = new BigDecimal("300000");

    @Override
    public void balance(BalanceDTO balanceDTO) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        // Поиск счета
        Optional<BankAccount> accountOpt = repository.findByAccountNumber(balanceDTO.getAccountNumber());
        if (accountOpt.isEmpty()) {
            auditLogService.logAction(userId, "баланс", "ошибка: аккаунт " + balanceDTO.getAccountNumber() + " не найден");
            throw new IllegalArgumentException("Счет не найден: " + balanceDTO.getAccountNumber());
        }
        BankAccount account = accountOpt.get();

        // Проверка валюты
        if (balanceDTO.getCurrency() != account.getCurrency()) {
            auditLogService.logAction(userId, "баланс", "ошибка: несоответствие валют, ожидал " + account.getCurrency() + ", получил " + balanceDTO.getCurrency());
            throw new IllegalArgumentException("Валюта пополнения должна совпадать с валютой счета: " + account.getCurrency());
        }

        // Проверка лимита и расчет комиссии
        BigDecimal amountInKZT = convertToKZT(balanceDTO.getAmount(), balanceDTO.getCurrency());

        BigDecimal commission = BigDecimal.ZERO;
        if (amountInKZT.compareTo(MAX_DEPOSIT_LIMIT_KZT) > 0) {
            // Если превышает лимит → комиссия 2%
            commission = balanceDTO.getAmount().multiply(BigDecimal.valueOf(0.02));
        }

        // Итоговая сумма с учетом комиссии
        BigDecimal totalCredit = balanceDTO.getAmount().subtract(commission);

        // Инициализация баланса, если он null
        BigDecimal currentBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;

        // Обновление баланса
        account.setBalance(currentBalance.add(totalCredit));
        repository.save(account);

        // Логирование успешного пополнения
        auditLogService.logAction(userId, "баланс",
                "успешно : пополнения баланса " + balanceDTO.getAmount() + " " + balanceDTO.getCurrency() +
                        " to account " + balanceDTO.getAccountNumber() +
                        (commission.compareTo(BigDecimal.ZERO) > 0 ? " (комиссия " + commission + ")" : "")
        );

        // Логирование активности в customer-service
        String activityUrl = customerServiceConfig.getUrl().replace("/exists/", "/") + account.getClientId() + "/log-activity";
        try {
            String token = jwtUtils.generateToken(userId);
            log.debug("Отправка токена для депозитной активности: {}", token);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> activityMap = Map.of(
                    "тип действия", "баланс",
                    "детали", "Аккаунт: " + balanceDTO.getAccountNumber() +
                            ", Сумма: " + balanceDTO.getAmount() + " " + balanceDTO.getCurrency() +
                            (commission.compareTo(BigDecimal.ZERO) > 0 ? ", Комиссия: " + commission : "")
            );
            HttpEntity<Map<String, String>> postEntity = new HttpEntity<>(activityMap, headers);

            restTemplate.exchange(activityUrl, HttpMethod.POST, postEntity, ApiResponse.class);
        } catch (Exception e) {
            log.error("Ошибка при логировании активности в customer-service: {}", e.getMessage());
            auditLogService.logAction(userId, "активность действия баланса", "ошибка: " + e.getMessage());
        }
    }


    private BigDecimal convertToKZT(BigDecimal amount, Currency currency) {
        if (currency == Currency.KZT) return amount;
        String key = currency.name() + "_TO_KZT";
        BigDecimal rate = TransferServiceImpl.EXCHANGE_RATES.getOrDefault(key, BigDecimal.ONE);
        return amount.multiply(rate);
    }
}