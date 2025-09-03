package kz.nurbank.practice.customer_service.repository;

import kz.nurbank.practice.customer_service.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByIin(String iin);
}
