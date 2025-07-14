package com.store.mgmt.pos.controller;

import com.store.mgmt.inventory.model.dto.ProductDTO;
import com.store.mgmt.pos.model.dto.TransactionDTO;
import com.store.mgmt.pos.service.PosService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos")
@Tag(name = "POS Transactions", description = "Operations related to Point-of-Sale transactions")
public class PosController {
    private final PosService posService;

    public PosController(PosService posService) {
        this.posService = posService;
    }

    @PostMapping("/transactions")
    @PreAuthorize("hasAuthority('PERM_CREATE_TRANSACTION')")
    @Operation(
            summary = "Create a new transaction",
            description = "Creates a new Point-of-Sale transaction with the provided details.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Transaction created successfully",
                            content = @Content(schema = @Schema(implementation = TransactionDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input or missing required fields",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'PERM_CREATE_TRANSACTION' authority",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<TransactionDTO> createTransaction(
            @Parameter(description = "Transaction details to be created", required = true)
            @RequestBody TransactionDTO request) {
        TransactionDTO transaction = posService.createTransaction(request);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/transactions/{id}")
    @PreAuthorize("hasAuthority('PERM_VIEW_TRANSACTION')")
    @Operation(
            summary = "Get a transaction by ID",
            description = "Retrieves a single transaction based on its unique identifier.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Transaction retrieved successfully",
                            content = @Content(schema = @Schema(implementation = TransactionDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Transaction not found",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'PERM_VIEW_TRANSACTION' authority",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<TransactionDTO> getTransaction(
            @Parameter(description = "Unique ID of the transaction to retrieve", required = true)
            @PathVariable UUID id) {
        TransactionDTO transaction = posService.getTransaction(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAuthority('PERM_VIEW_ALL_TRANSACTIONS')")
    @Operation(
            summary = "Get all transactions",
            description = "Retrieves a list of all available Point-of-Sale transactions.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of transactions retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                            array = @ArraySchema(schema = @Schema(implementation = TransactionDTO.class))

                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'PERM_VIEW_ALL_TRANSACTIONS' authority",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        List<TransactionDTO> transactions = posService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/transactions/{id}")
    @PreAuthorize("hasAuthority('PERM_UPDATE_TRANSACTION')")
    @Operation(
            summary = "Update an existing transaction",
            description = "Updates the details of an existing transaction identified by its ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Transaction updated successfully",
                            content = @Content(schema = @Schema(implementation = TransactionDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input or missing required fields",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Transaction not found",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'PERM_UPDATE_TRANSACTION' authority",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<TransactionDTO> updateTransaction(
            @Parameter(description = "Unique ID of the transaction to update", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated transaction details", required = true)
            @RequestBody TransactionDTO request) {
        TransactionDTO transaction = posService.updateTransaction(id, request);
        return ResponseEntity.ok(transaction);
    }

    @DeleteMapping("/transactions/{id}")
    @PreAuthorize("hasAuthority('PERM_VOID_TRANSACTION')")
    @Operation(
            summary = "Void/Delete a transaction",
            description = "Marks a transaction as void or deletes it based on its unique identifier. This is typically a soft delete or status change.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Transaction deleted/voided successfully",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Transaction not found",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'PERM_VOID_TRANSACTION' authority",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "Unique ID of the transaction to void/delete", required = true)
            @PathVariable UUID id) {
        posService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}