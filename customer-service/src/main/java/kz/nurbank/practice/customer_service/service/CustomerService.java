package kz.nurbank.practice.customer_service.service;

import kz.nurbank.practice.customer_service.dto.RegistrationDTO; // Изменили на новый DTO
import kz.nurbank.practice.customer_service.model.Customer;
import kz.nurbank.practice.customer_service.model.CustomerActivity;
import kz.nurbank.practice.customer_service.model.Role; // Для создания User
import kz.nurbank.practice.customer_service.model.User; // Для создания User

import java.util.List;
import java.util.Optional;

public interface CustomerService {

    Customer save(RegistrationDTO dto);

    Optional<Customer> findByIin(String iin);

    Optional<Customer> findById(Long id);

    void logCustomerActivity(Long customerId, String actionType, String details);

    void updateLastLogin(Long customerId);

    List<CustomerActivity> getCustomerActivities(Long customerId);
}