package kz.nurbank.practice.bank_service.mapper;

import kz.nurbank.practice.bank_service.dto.BankAccountDTO;
import kz.nurbank.practice.bank_service.model.BankAccount;

public class BankAccountMapper {
    public static BankAccountDTO toDTO(BankAccount entity) {
        return BankAccountDTO.builder()
                .accountNumber(entity.getAccountNumber())
                .clientId(entity.getClientId())
                .currency(entity.getCurrency())
                .build();
    }
}
