package kz.nurbank.practice.bank_service.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

@Data
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "customer.service")
public class CustomerServiceConfig {
    private String url;
}
