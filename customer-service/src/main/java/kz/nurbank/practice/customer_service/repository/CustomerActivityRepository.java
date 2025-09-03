package kz.nurbank.practice.customer_service.repository;

import kz.nurbank.practice.customer_service.model.CustomerActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerActivityRepository extends JpaRepository<CustomerActivity, Long> {
    List<CustomerActivity> findByCustomerId(Long customerId);
}