package kz.nurbank.practice.bank_service.service;

import kz.nurbank.practice.bank_service.model.BankAccount;
import kz.nurbank.practice.bank_service.model.User;
import kz.nurbank.practice.bank_service.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("securityService")
@RequiredArgsConstructor
public class SecurityService {

    private final BankAccountRepository bankAccountRepository;

    public boolean isOwnCustomer(Long clientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            return false;
        }
        return user.getCustomerId() != null && user.getCustomerId().equals(clientId);
    }

    public boolean canTransferFromAccount(String fromAccountNumber) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            return false;
        }
        Optional<BankAccount> accountOpt = bankAccountRepository.findByAccountNumber(fromAccountNumber);
        return accountOpt.isPresent() && user.getCustomerId() != null && user.getCustomerId().equals(accountOpt.get().getClientId());
    }
}