package com.store.mgmt.pos.service;

import com.store.mgmt.pos.model.dto.TransactionDTO;

import java.util.List;
import java.util.UUID;

public interface PosService {
    TransactionDTO createTransaction(TransactionDTO request);
    TransactionDTO getTransaction(UUID id);
    List<TransactionDTO> getAllTransactions();
    TransactionDTO updateTransaction(UUID id, TransactionDTO request);
    void deleteTransaction(UUID id);
}
