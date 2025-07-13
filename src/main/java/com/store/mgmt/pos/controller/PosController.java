package com.store.mgmt.pos.controller;

import com.store.mgmt.pos.model.dto.TransactionDTO;
import com.store.mgmt.pos.service.PosService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos")
public class PosController {
    private final PosService posService;

    public PosController(PosService posService) {
        this.posService = posService;
    }

    @PostMapping("/transactions")
    @PreAuthorize("hasAuthority('PERM_CREATE_TRANSACTION')")
    public ResponseEntity<TransactionDTO> createTransaction(@RequestBody TransactionDTO request) {
        TransactionDTO transaction = posService.createTransaction(request);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/transactions/{id}")
    @PreAuthorize("hasAuthority('PERM_VIEW_TRANSACTION')")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable UUID id) {
        TransactionDTO transaction = posService.getTransaction(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAuthority('PERM_VIEW_ALL_TRANSACTIONS')")
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        List<TransactionDTO> transactions = posService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/transactions/{id}")
    @PreAuthorize("hasAuthority('PERM_UPDATE_TRANSACTION')")
    public ResponseEntity<TransactionDTO> updateTransaction(@PathVariable UUID id, @RequestBody TransactionDTO request) {
        TransactionDTO transaction = posService.updateTransaction(id, request);
        return ResponseEntity.ok(transaction);
    }

    @DeleteMapping("/transactions/{id}")
    @PreAuthorize("hasAuthority('PERM_VOID_TRANSACTION')")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id) {
        posService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}