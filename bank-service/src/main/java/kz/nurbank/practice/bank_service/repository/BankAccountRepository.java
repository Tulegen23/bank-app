package kz.nurbank.practice.bank_service.repository;

import kz.nurbank.practice.bank_service.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByClientId(Long clientId);
    Optional<BankAccount> findByAccountNumber(String accountNumber);
}
