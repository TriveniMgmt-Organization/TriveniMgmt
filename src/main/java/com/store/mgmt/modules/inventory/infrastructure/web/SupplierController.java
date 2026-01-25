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

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Supplier management using Clean Architecture.
 */
@RestController
@RequestMapping("/api/v2/inventory/suppliers")
@Tag(name = "Supplier Management (v2)", description = "Clean Architecture supplier endpoints")
public class SupplierController {

    private static final Logger log = LoggerFactory.getLogger(SupplierController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public SupplierController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    // ==================== Query Endpoints ====================

    @GetMapping
    @Operation(summary = "Get all suppliers", description = "Retrieves a list of all suppliers for the organization")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suppliers retrieved successfully")
    })
    @PreAuthorize("hasAuthority('SUPPLIER_READ')")
    public ResponseEntity<List<SupplierResponseDTO>> getAllSuppliers() {
        UUID organizationId = TenantContext.current().organizationId();
        log.debug("Getting all suppliers for organization: {}", organizationId);
        List<SupplierResponseDTO> result = queryBus.dispatch(new GetAllSuppliersQuery(organizationId));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID", description = "Retrieves a supplier by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplier retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Supplier not found")
    })
    @PreAuthorize("hasAuthority('SUPPLIER_READ')")
    public ResponseEntity<SupplierResponseDTO> getSupplierById(@PathVariable UUID id) {
        UUID organizationId = TenantContext.current().organizationId();
        log.debug("Getting supplier by ID: {}", id);
        try {
            SupplierResponseDTO result = queryBus.dispatch(new GetSupplierByIdQuery(id, organizationId));
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== Command Endpoints ====================

    @PostMapping
    @Operation(summary = "Create a new supplier", description = "Creates a new supplier")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Supplier created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Supplier with name already exists")
    })
    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    public ResponseEntity<SupplierResponseDTO> createSupplier(
            @Valid @RequestBody CreateSupplierRequestDTO request
    ) {
        UUID organizationId = TenantContext.current().organizationId();
        log.info("Creating supplier: {} for organization: {}", request.name(), organizationId);
        try {
            CreateSupplierCommand cmd = new CreateSupplierCommand(
                    organizationId,
                    request.name(),
                    request.contactPerson(),
                    request.email(),
                    request.phone(),
                    request.address(),
                    request.accountNumber()
            );
            SupplierResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            log.warn("Supplier creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a supplier", description = "Updates an existing supplier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplier updated successfully"),
            @ApiResponse(responseCode = "404", description = "Supplier not found"),
            @ApiResponse(responseCode = "409", description = "Supplier with name already exists")
    })
    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    public ResponseEntity<SupplierResponseDTO> updateSupplier(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSupplierRequestDTO request
    ) {
        UUID organizationId = TenantContext.current().organizationId();
        log.info("Updating supplier: {}", id);
        try {
            UpdateSupplierCommand cmd = new UpdateSupplierCommand(
                    id,
                    organizationId,
                    request.name(),
                    request.contactPerson(),
                    request.email(),
                    request.phone(),
                    request.address(),
                    request.accountNumber()
            );
            SupplierResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Supplier update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a supplier", description = "Soft deletes a supplier")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Supplier deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Supplier not found")
    })
    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    public ResponseEntity<Void> deleteSupplier(@PathVariable UUID id) {
        UUID organizationId = TenantContext.current().organizationId();
        log.info("Deleting supplier: {}", id);
        try {
            commandBus.dispatch(new DeleteSupplierCommand(id, organizationId));
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
