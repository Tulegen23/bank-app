package kz.nurbank.practice.bank_service.service;

import io.github.resilience4j.retry.Retry;
import kz.nurbank.practice.bank_service.config.CustomerServiceConfig;
import kz.nurbank.practice.bank_service.dto.ApiResponse;
import kz.nurbank.practice.bank_service.dto.TransferDTO;
import kz.nurbank.practice.bank_service.model.BankAccount;
import kz.nurbank.practice.bank_service.model.Currency;
import kz.nurbank.practice.bank_service.repository.BankAccountRepository;
import kz.nurbank.practice.bank_service.repository.UserRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    private static final Logger log = LoggerFactory.getLogger(TransferServiceImpl.class);

    private final BankAccountRepository accountRepository;
    private final RestTemplate restTemplate;
    private final CustomerServiceConfig customerServiceConfig;
    private final Retry retry;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    public static final Map<String, BigDecimal> EXCHANGE_RATES = new HashMap<>();

    static {
        EXCHANGE_RATES.put("USD_TO_KZT", new BigDecimal("540"));
        EXCHANGE_RATES.put("EUR_TO_KZT", new BigDecimal("630"));
        EXCHANGE_RATES.put("KZT_TO_USD", BigDecimal.ONE.divide(new BigDecimal("540"), 4, RoundingMode.HALF_UP));
        EXCHANGE_RATES.put("KZT_TO_EUR", BigDecimal.ONE.divide(new BigDecimal("630"), 4, RoundingMode.HALF_UP));
        EXCHANGE_RATES.put("USD_TO_EUR", new BigDecimal("0.8568").divide(new BigDecimal("1.1672"), 4, RoundingMode.HALF_UP));
        EXCHANGE_RATES.put("EUR_TO_USD", new BigDecimal("1.1672").divide(new BigDecimal("0.8568"), 4, RoundingMode.HALF_UP));
    }

    private static final BigDecimal MAX_TRANSFER_LIMIT_KZT = new BigDecimal("300000");

    @Override
    @Transactional
    public String performTransfer(TransferDTO request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<BankAccount> fromAccountOpt = accountRepository.findByAccountNumber(request.getFromAccountNumber());
        if (fromAccountOpt.isEmpty()) {
            throw new IllegalArgumentException("Номер счета отправителя не найден: " + request.getFromAccountNumber());
        }

        BankAccount fromAccount = fromAccountOpt.get();
        BigDecimal amount = request.getAmount();
        Currency fromCurrency = fromAccount.getCurrency();

        // считаем комиссию
        BigDecimal commission = calculateCommission(amount, request.isExternal());
        BigDecimal totalDebit = amount.add(commission);

        if (fromAccount.getBalance().compareTo(totalDebit) < 0) {
            throw new IllegalArgumentException("Баланс недостаточен");
        }

        BigDecimal amountInKZT = convertToKZT(amount, fromCurrency);
        if (amountInKZT.compareTo(MAX_TRANSFER_LIMIT_KZT) > 0) {
            throw new IllegalArgumentException("Сумма операции превышает лимит");
        }

        if (request.isExternal()) {
            // внешний перевод
            fromAccount.setBalance(fromAccount.getBalance().subtract(totalDebit));
            accountRepository.save(fromAccount);

            auditLogService.logAction(userId, "внешний перевод",
                    "усрешно: " + amount + " " + fromCurrency + " на внешний счет " + request.getToAccountNumber());

            return "Внешний перевод успешен: " + request.getToAccountNumber();
        } else {
            // внутренний перевод
            Optional<BankAccount> toAccountOpt = accountRepository.findByAccountNumber(request.getToAccountNumber());
            if (toAccountOpt.isEmpty()) {
                throw new IllegalArgumentException("Номер счета получателя не найден: " + request.getToAccountNumber());
            }

            BankAccount toAccount = toAccountOpt.get();
            checkClientExists(toAccount.getClientId());

            Currency toCurrency = toAccount.getCurrency();
            BigDecimal convertedAmount = (fromCurrency == toCurrency)
                    ? amount
                    : convertCurrency(amount, fromCurrency, toCurrency);

            fromAccount.setBalance(fromAccount.getBalance().subtract(totalDebit));
            toAccount.setBalance(toAccount.getBalance().add(convertedAmount));

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            String transferType = fromAccount.getClientId().equals(toAccount.getClientId())
                    ? "собственные счета" : "разные клиенты";

            auditLogService.logAction(userId, "внутренний перевод (" + transferType + ")",
                    "успех: " + amount + " " + fromCurrency + " к " + request.getToAccountNumber());

            // логируем активность отправителя
            String activityUrl = customerServiceConfig.getUrl().replace("/exists/", "/")
                    + fromAccount.getClientId() + "/log-activity";
            try {
                String token = jwtUtils.generateToken(userId);
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + token);
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, String> activityMap = Map.of(
                        "тип действия", "перевод",
                        "детали", "От: " + request.getFromAccountNumber()
                                + ", к: " + request.getToAccountNumber()
                                + ", Сумма: " + amount + " " + fromCurrency
                );

                HttpEntity<Map<String, String>> postEntity = new HttpEntity<>(activityMap, headers);
                restTemplate.exchange(activityUrl, HttpMethod.POST, postEntity, ApiResponse.class);
            } catch (Exception e) {
                log.error("Логирование к Customer Service безуспешно: {}", e.getMessage());
                auditLogService.logAction(userId, "активность переводов", "ошибка: " + e.getMessage());
            }

            // логируем активность получателя
            String activityUrl2 = customerServiceConfig.getUrl().replace("/exists/", "/")
                    + toAccount.getClientId() + "/log-activity";
            try {
                String token = jwtUtils.generateToken(userId);
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + token);
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, String> activityMap = Map.of(
                        "тип действия", "перевод",
                        "детали", "От: " + request.getFromAccountNumber()
                                + ", К: " + request.getToAccountNumber()
                                + ", Сумма: " + amount + " " + fromCurrency
                );

                HttpEntity<Map<String, String>> postEntity = new HttpEntity<>(activityMap, headers);
                restTemplate.exchange(activityUrl2, HttpMethod.POST, postEntity, ApiResponse.class);
            } catch (Exception e) {
                log.error("Логирование к Customer Service неправильно: {}", e.getMessage());
                auditLogService.logAction(userId, "активность переводов", "ошибка: " + e.getMessage());
            }

            return "Внутренний перевод успешен: " + request.getToAccountNumber();
        }
    }

    // 🔥 тут переписал комиссию
    private BigDecimal calculateCommission(BigDecimal amount, boolean isExternal) {
        if (!isExternal) {
            // внутри банка комиссия 0
            return BigDecimal.ZERO;
        } else {
            // внешний перевод — комиссия 3%
            return amount.multiply(new BigDecimal("0.03")).setScale(2, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal convertCurrency(BigDecimal amount, Currency from, Currency to) {
        if (from == to) return amount;
        String key = from.name() + "_TO_" + to.name();
        BigDecimal rate = EXCHANGE_RATES.getOrDefault(key, BigDecimal.ONE);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal convertToKZT(BigDecimal amount, Currency currency) {
        if (currency == Currency.KZT) return amount;
        String key = currency.name() + "_TO_KZT";
        BigDecimal rate = EXCHANGE_RATES.getOrDefault(key, BigDecimal.ONE);
        return amount.multiply(rate);
    }

    private void checkClientExists(Long clientId) {
        String checkUrl = customerServiceConfig.getUrl() + clientId;
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        Boolean exists = Retry.decorateSupplier(retry, () -> {
            try {
                String token = jwtUtils.generateToken(userId);
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + token);
                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<ApiResponse<Boolean>> responseEntity =
                        restTemplate.exchange(checkUrl, HttpMethod.GET, entity,
                                new ParameterizedTypeReference<ApiResponse<Boolean>>() {});

                ApiResponse<Boolean> response = responseEntity.getBody();
                return response != null && response.getData() != null && response.getData();
            } catch (HttpServerErrorException e) {
                log.error("Ошибка тестирования клиента {}: {}", clientId, e.getMessage());
                throw e;
            }
        }).get();

        if (!exists) {
            throw new IllegalArgumentException("Клиент не найден: " + clientId);
        }
    }
}
