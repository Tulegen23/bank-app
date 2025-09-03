package kz.nurbank.practice.customer_service.service;

import jakarta.annotation.PostConstruct;
import kz.nurbank.practice.customer_service.dto.RegistrationDTO;
import kz.nurbank.practice.customer_service.model.Customer;
import kz.nurbank.practice.customer_service.model.CustomerActivity;
import kz.nurbank.practice.customer_service.model.Role;
import kz.nurbank.practice.customer_service.model.User;
import kz.nurbank.practice.customer_service.repository.CustomerActivityRepository;
import kz.nurbank.practice.customer_service.repository.CustomerRepository;
import kz.nurbank.practice.customer_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initAdmin() {
        userRepository.findByUsername("admin").orElseGet(() -> {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .customerId(null)
                    .build();
            return userRepository.save(admin);
        });
    }

    //В этом функции принимает данные, хэширует пароль и сохраняет их в базе данных для клиента.
    @Override
    public Customer save(RegistrationDTO dto) {
        Customer customer = Customer.builder()
                .fullName(dto.getFullName())
                .iin(dto.getIin())
                .phone(dto.getPhone())
                .build();
        customer = repository.save(customer);

        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        User user = User.builder()
                .username(dto.getUsername())
                .password(hashedPassword)
                .role(Role.USER)
                .customerId(customer.getId())
                .build();
        userRepository.save(user);

        return customer;
    }

    //Тут с помощью функции jpa ищут клиента по иин.
    @Override
    public Optional<Customer> findByIin(String iin) {
        return repository.findByIin(iin);
    }

    //С помощью jpa ищут клиента по id.
    @Override
    public Optional<Customer> findById(Long id) {
        return repository.findById(id);
    }

    //Тут сохраняем история клиента.
    @Override
    @Async
    public void logCustomerActivity(Long customerId, String actionType, String details) {
        CustomerActivity activity = CustomerActivity.builder()
                .customerId(customerId)
                .actionType(actionType)
                .timestamp(ZonedDateTime.now())
                .details(details)
                .build();
        activityRepository.save(activity);
    }

    @Override
    public void updateLastLogin(Long customerId) {
        Optional<Customer> customerOpt = repository.findById(customerId);
        customerOpt.ifPresent(customer -> {
            customer.setLastLogin(ZonedDateTime.now());
            repository.save(customer);
        });
    }

    //С помощью id выводим историю клиента
    @Override
    public List<CustomerActivity> getCustomerActivities(Long customerId) {
        return activityRepository.findByCustomerId(customerId);
    }
}