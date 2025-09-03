package kz.nurbank.practice.bank_service.service;

import kz.nurbank.practice.bank_service.dto.BalanceDTO;
import kz.nurbank.practice.bank_service.dto.BankAccountDTO;
import kz.nurbank.practice.bank_service.model.Currency;

import java.math.BigDecimal;
import java.util.List;

public interface BankAccountService {
    String createAccount(Long clientId, Currency currency);
    List<BankAccountDTO> getAccountsByClientId(Long clientId);
    void balance(BalanceDTO balanceDTO);
}
