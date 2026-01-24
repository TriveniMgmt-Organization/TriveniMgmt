package com.store.mgmt.modules.inventory.infrastructure.web;

import com.store.mgmt.modules.inventory.application.command.*;
import com.store.mgmt.modules.inventory.application.dto.*;
import com.store.mgmt.modules.inventory.application.query.*;
import com.store.mgmt.shared.infrastructure.CommandBus;
import com.store.mgmt.shared.infrastructure.QueryBus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for Stock Transaction management using Clean Architecture.
 */
@RestController
@RequestMapping("/api/v2/inventory/transactions")
@Tag(name = "Stock Transactions (v2)", description = "Clean Architecture stock transaction endpoints")
public class StockTransactionController {

    private static final Logger log = LoggerFactory.getLogger(StockTransactionController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public StockTransactionController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    // ==================== Query Endpoints ====================

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieves a stock transaction by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<StockTransactionResponseDTO> getTransactionById(@PathVariable UUID id) {
        log.debug("Getting transaction by ID: {}", id);
        try {
            StockTransactionResponseDTO result = queryBus.dispatch(new GetStockTransactionByIdQuery(id));
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-inventory-item/{inventoryItemId}")
    @Operation(summary = "Get transactions by inventory item", description = "Retrieves all transactions for an inventory item")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    })
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<List<StockTransactionResponseDTO>> getTransactionsByInventoryItem(
            @PathVariable UUID inventoryItemId
    ) {
        log.debug("Getting transactions for inventory item: {}", inventoryItemId);
        List<StockTransactionResponseDTO> result = queryBus.dispatch(
                new GetTransactionsByInventoryItemQuery(inventoryItemId)
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-date-range")
    @Operation(summary = "Get transactions by date range", description = "Retrieves transactions within a date range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    })
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<List<StockTransactionResponseDTO>> getTransactionsByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        log.debug("Getting transactions between {} and {}", startDate, endDate);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        List<StockTransactionResponseDTO> result = queryBus.dispatch(
                new GetTransactionsByDateRangeQuery(start, end)
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stock-level/{inventoryItemId}")
    @Operation(summary = "Get stock level", description = "Retrieves current stock level for an inventory item")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock level retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Stock level not found")
    })
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<StockLevelResponseDTO> getStockLevel(@PathVariable UUID inventoryItemId) {
        log.debug("Getting stock level for inventory item: {}", inventoryItemId);
        try {
            StockLevelResponseDTO result = queryBus.dispatch(new GetStockLevelQuery(inventoryItemId));
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stock-levels/by-variant/{variantId}")
    @Operation(summary = "Get stock levels by variant", description = "Retrieves all stock levels for a variant across locations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock levels retrieved successfully")
    })
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<List<StockLevelResponseDTO>> getStockLevelsByVariant(@PathVariable UUID variantId) {
        log.debug("Getting stock levels for variant: {}", variantId);
        List<StockLevelResponseDTO> result = queryBus.dispatch(new GetStockLevelsByVariantQuery(variantId));
        return ResponseEntity.ok(result);
    }

    // ==================== Command Endpoints ====================

    @PostMapping
    @Operation(summary = "Create a stock transaction", description = "Creates a new stock transaction and updates stock levels")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Inventory item not found")
    })
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    public ResponseEntity<StockTransactionResponseDTO> createTransaction(
            @Valid @RequestBody CreateStockTransactionRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Creating stock transaction for inventory item: {}, type: {}, delta: {}",
                request.inventoryItemId(), request.type(), request.quantityDelta());
        try {
            // Get user ID from authenticated principal if available
            UUID userId = null;
            // Note: In a real implementation, you would extract the user ID from the UserDetails

            CreateStockTransactionCommand cmd = new CreateStockTransactionCommand(
                    request.inventoryItemId(),
                    request.type(),
                    request.quantityDelta(),
                    request.referenceType(),
                    request.referenceId(),
                    request.reason(),
                    request.fromLocationId(),
                    request.toLocationId(),
                    request.notes(),
                    userId
            );
            StockTransactionResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Stock transaction creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
