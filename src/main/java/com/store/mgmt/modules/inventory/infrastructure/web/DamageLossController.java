package com.store.mgmt.modules.inventory.infrastructure.web;

import com.store.mgmt.modules.inventory.application.command.*;
import com.store.mgmt.modules.inventory.application.dto.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for Damage/Loss management using Clean Architecture.
 */
@RestController
@RequestMapping("/api/v2/inventory/damage-loss")
@Tag(name = "Damage/Loss Management (v2)", description = "Clean Architecture damage/loss endpoints")
public class DamageLossController {

    private static final Logger log = LoggerFactory.getLogger(DamageLossController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public DamageLossController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    // ==================== Query Endpoints ====================

    @GetMapping("/{id}")
    @Operation(summary = "Get damage/loss by ID", description = "Retrieves a damage/loss record by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Record retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Record not found")
    })
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<DamageLossResponseDTO> getDamageLossById(@PathVariable UUID id) {
        UUID organizationId = TenantContext.current().organizationId();
        log.debug("Getting damage/loss by ID: {}", id);
        try {
            DamageLossResponseDTO result = queryBus.dispatch(new GetDamageLossByIdQuery(id, organizationId));
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get damage/loss records", description = "Retrieves damage/loss records with optional filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Records retrieved successfully")
    })
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<List<DamageLossResponseDTO>> getDamageLossRecords(
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        UUID storeId = TenantContext.current().storeId();
        log.debug("Getting damage/loss records, storeId: {}, locationId: {}, startDate: {}, endDate: {}",
                storeId, locationId, startDate, endDate);
        List<DamageLossResponseDTO> result = queryBus.dispatch(
                new GetDamageLossRecordsQuery(storeId, locationId, startDate, endDate)
        );
        return ResponseEntity.ok(result);
    }

    // ==================== Command Endpoints ====================

    @PostMapping
    @Operation(summary = "Record damage/loss", description = "Records a new damage/loss and decrements stock")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Record created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Variant or location not found")
    })
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    public ResponseEntity<DamageLossResponseDTO> recordDamageLoss(
            @Valid @RequestBody CreateDamageLossRequestDTO request
    ) {
        UUID organizationId = TenantContext.current().organizationId();
        UUID storeId = TenantContext.current().storeId();
        UUID userId = TenantContext.current().userId();
        log.info("Recording damage/loss for variant: {}, location: {}, quantity: {}",
                request.variantId(), request.locationId(), request.quantity());
        try {
            RecordDamageLossCommand cmd = new RecordDamageLossCommand(
                    organizationId,
                    storeId,
                    request.variantId(),
                    request.locationId(),
                    request.quantity(),
                    request.reason(),
                    request.notes(),
                    userId
            );
            DamageLossResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Damage/loss recording failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
