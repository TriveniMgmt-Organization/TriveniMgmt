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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Batch/Lot management using Clean Architecture.
 */
@RestController
@RequestMapping("/api/v2/inventory/batch-lots")
@Tag(name = "Batch/Lot Management (v2)", description = "Clean Architecture batch/lot endpoints")
public class BatchLotController {

    private static final Logger log = LoggerFactory.getLogger(BatchLotController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public BatchLotController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    // ==================== Query Endpoints ====================

    @GetMapping
    @Operation(summary = "Get all batch/lots", description = "Retrieves a list of all batch/lots")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Batch/lots retrieved successfully")
    })
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_READ')")
    public ResponseEntity<List<BatchLotResponseDTO>> getAllBatchLots(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    ) {
        log.debug("Getting all batch/lots, includeInactive: {}", includeInactive);
        List<BatchLotResponseDTO> result = queryBus.dispatch(new GetAllBatchLotsQuery(includeInactive));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get batch/lot by ID", description = "Retrieves a batch/lot by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Batch/lot retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Batch/lot not found")
    })
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_READ')")
    public ResponseEntity<BatchLotResponseDTO> getBatchLotById(@PathVariable UUID id) {
        log.debug("Getting batch/lot by ID: {}", id);
        try {
            BatchLotResponseDTO result = queryBus.dispatch(new GetBatchLotByIdQuery(id));
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/expiring")
    @Operation(summary = "Get expiring batch/lots", description = "Retrieves batch/lots expiring within the specified number of days")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expiring batch/lots retrieved successfully")
    })
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_READ')")
    public ResponseEntity<List<BatchLotResponseDTO>> getExpiringBatchLots(
            @RequestParam(required = false, defaultValue = "30") int daysAhead
    ) {
        log.debug("Getting batch/lots expiring within {} days", daysAhead);
        List<BatchLotResponseDTO> result = queryBus.dispatch(new GetExpiringBatchLotsQuery(daysAhead));
        return ResponseEntity.ok(result);
    }

    // ==================== Command Endpoints ====================

    @PostMapping
    @Operation(summary = "Create a new batch/lot", description = "Creates a new batch/lot")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Batch/lot created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Supplier not found"),
            @ApiResponse(responseCode = "409", description = "Batch number already exists")
    })
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_WRITE')")
    public ResponseEntity<BatchLotResponseDTO> createBatchLot(
            @Valid @RequestBody CreateBatchLotRequestDTO request
    ) {
        log.info("Creating batch/lot: {}", request.batchNumber());
        try {
            CreateBatchLotCommand cmd = new CreateBatchLotCommand(
                    request.batchNumber(),
                    request.manufactureDate(),
                    request.expiryDate(),
                    request.supplierId()
            );
            BatchLotResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Batch/lot creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a batch/lot", description = "Deactivates a batch/lot (soft delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Batch/lot deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Batch/lot not found")
    })
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_WRITE')")
    public ResponseEntity<Void> deactivateBatchLot(@PathVariable UUID id) {
        log.info("Deactivating batch/lot: {}", id);
        try {
            commandBus.dispatch(new DeactivateBatchLotCommand(id));
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
