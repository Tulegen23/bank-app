package kz.nurbank.practice.bank_service.service;

import kz.nurbank.practice.bank_service.dto.BalanceDTO;
import kz.nurbank.practice.bank_service.dto.TransferDTO;

import java.util.concurrent.CompletableFuture;

public interface TransferService {
    String performTransfer(TransferDTO transferDTO);
}
