package com.store.mgmt.modules.inventory.infrastructure.web;

import com.store.mgmt.modules.inventory.application.command.ProcessSaleCommand;
import com.store.mgmt.modules.inventory.application.dto.CreateSaleRequestDTO;
import com.store.mgmt.modules.inventory.application.dto.SaleResponseDTO;
import com.store.mgmt.modules.inventory.application.query.*;
import com.store.mgmt.shared.infrastructure.CommandBus;
import com.store.mgmt.shared.infrastructure.QueryBus;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for Sales management using Clean Architecture.
 */
@RestController
@RequestMapping("/api/v2/inventory/sales")
@Tag(name = "Sales (v2)", description = "Clean Architecture sales endpoints")
public class SaleController {

    private static final Logger log = LoggerFactory.getLogger(SaleController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public SaleController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    // ==================== Query Endpoints ====================

    @GetMapping("/{id}")
    @Operation(summary = "Get sale by ID", description = "Retrieves a sale by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sale retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Sale not found")
    })
    @PreAuthorize("hasAuthority('SALE_READ')")
    public ResponseEntity<SaleResponseDTO> getSaleById(@PathVariable UUID id) {
        UUID storeId = TenantContext.current().storeId();
        log.debug("Getting sale by ID: {}", id);
        try {
            SaleResponseDTO result = queryBus.dispatch(new GetSaleByIdQuery(id, storeId));
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get sales by date range", description = "Retrieves sales within a date range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sales retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    @PreAuthorize("hasAuthority('SALE_READ')")
    public ResponseEntity<List<SaleResponseDTO>> getSalesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        UUID storeId = TenantContext.current().storeId();
        log.debug("Getting sales for store {} between {} and {}", storeId, startDate, endDate);

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        List<SaleResponseDTO> result = queryBus.dispatch(
                new GetSalesByDateRangeQuery(storeId, startDate, endDate)
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/product/{productTemplateId}")
    @Operation(summary = "Get sales for a product", description = "Retrieves sales containing a specific product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sales retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PreAuthorize("hasAuthority('SALE_READ')")
    public ResponseEntity<List<SaleResponseDTO>> getSalesForProduct(@PathVariable UUID productTemplateId) {
        UUID storeId = TenantContext.current().storeId();
        log.debug("Getting sales for product template {} in store {}", productTemplateId, storeId);
        try {
            List<SaleResponseDTO> result = queryBus.dispatch(
                    new GetSalesForProductQuery(storeId, productTemplateId)
            );
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== Command Endpoints ====================

    @PostMapping
    @Operation(summary = "Process a sale", description = "Processes a new sale with FIFO stock allocation")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sale processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Related entity not found")
    })
    @PreAuthorize("hasAuthority('SALE_WRITE')")
    public ResponseEntity<SaleResponseDTO> processSale(
            @Valid @RequestBody CreateSaleRequestDTO request
    ) {
        UUID storeId = TenantContext.current().storeId();
        UUID userId = TenantContext.current().userId();
        log.info("Processing sale for store: {}", storeId);
        try {
            ProcessSaleCommand cmd = new ProcessSaleCommand(
                    storeId,
                    request.paymentMethod(),
                    request.transactionId(),
                    request.notes(),
                    request.items(),
                    userId
            );
            SaleResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Sale processing failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
