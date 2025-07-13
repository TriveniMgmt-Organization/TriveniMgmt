package com.store.mgmt.pos.service;

import com.store.mgmt.pos.model.entity.Transaction;
import com.store.mgmt.pos.model.dto.TransactionDTO;
import com.store.mgmt.pos.repository.TransactionRepository;
import com.store.mgmt.inventory.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PosServiceImpl implements PosService {
    private final TransactionRepository transactionRepository;
    private final InventoryService inventoryService;

    public PosServiceImpl(TransactionRepository transactionRepository, InventoryService inventoryService) {
        this.transactionRepository = transactionRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public TransactionDTO createTransaction(TransactionDTO request) {
        // Validate stock
        inventoryService.checkStock(request.getProductId(), request.getQuantity());

        // Calculate tax (e.g., 8% sales tax)
        BigDecimal price = inventoryService.getProductPrice(request.getProductId());
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(request.getQuantity()));
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.08));
        BigDecimal total = subtotal.add(tax);

        // Update inventory
        inventoryService.updateStock(request.getProductId(), -request.getQuantity());

        // Save transaction
        Transaction transaction = new Transaction();
        transaction.setProductId(request.getProductId());
        transaction.setQuantity(request.getQuantity());
        transaction.setTotal(total);
        transaction.setTax(tax);
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setUserId(request.getUserId());

        Transaction saved = transactionRepository.save(transaction);

        // Convert to DTO
        TransactionDTO response = new TransactionDTO();
        response.setId(saved.getId());
        response.setProductId(saved.getProductId());
        response.setQuantity(saved.getQuantity());
        response.setTotal(saved.getTotal());
        response.setTax(saved.getTax());
        response.setPaymentMethod(saved.getPaymentMethod());
        response.setTimestamp(saved.getTimestamp());
        response.setUserId(saved.getUserId());
        return response;
    }

    public TransactionDTO getTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setProductId(transaction.getProductId());
        dto.setQuantity(transaction.getQuantity());
        dto.setTotal(transaction.getTotal());
        dto.setTax(transaction.getTax());
        dto.setPaymentMethod(transaction.getPaymentMethod());
        dto.setTimestamp(transaction.getTimestamp());
        dto.setUserId(transaction.getUserId());
        return dto;
    }

    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll().stream().map(t -> {
            TransactionDTO dto = new TransactionDTO();
            dto.setId(t.getId());
            dto.setProductId(t.getProductId());
            dto.setQuantity(t.getQuantity());
            dto.setTotal(t.getTotal());
            dto.setTax(t.getTax());
            dto.setPaymentMethod(t.getPaymentMethod());
            dto.setTimestamp(t.getTimestamp());
            dto.setUserId(t.getUserId());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public TransactionDTO updateTransaction(UUID id, TransactionDTO request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Revert previous inventory change
        inventoryService.updateStock(transaction.getProductId(), transaction.getQuantity());

        // Update inventory with new quantity
        inventoryService.checkStock(request.getProductId(), request.getQuantity());
        inventoryService.updateStock(request.getProductId(), -request.getQuantity());

        // Recalculate total and tax
        BigDecimal price = inventoryService.getProductPrice(request.getProductId());
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(request.getQuantity()));
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.08));

        transaction.setProductId(request.getProductId());
        transaction.setQuantity(request.getQuantity());
        transaction.setTotal(subtotal.add(tax));
        transaction.setTax(tax);
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setUserId(request.getUserId());

        Transaction updated = transactionRepository.save(transaction);

        TransactionDTO response = new TransactionDTO();
        response.setId(updated.getId());
        response.setProductId(updated.getProductId());
        response.setQuantity(updated.getQuantity());
        response.setTotal(updated.getTotal());
        response.setTax(updated.getTax());
        response.setPaymentMethod(updated.getPaymentMethod());
        response.setTimestamp(updated.getTimestamp());
        response.setUserId(updated.getUserId());
        return response;
    }

    @Transactional
    public void deleteTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        // Revert inventory change
        inventoryService.updateStock(transaction.getProductId(), transaction.getQuantity());
        transactionRepository.deleteById(id);
    }
}
