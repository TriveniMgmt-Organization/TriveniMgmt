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
 * REST controller for Unit of Measure management using Clean Architecture.
 */
@RestController
@RequestMapping("/api/v2/inventory/units-of-measure")
@Tag(name = "Unit of Measure Management (v2)", description = "Clean Architecture unit of measure endpoints")
public class UnitOfMeasureController {

    private static final Logger log = LoggerFactory.getLogger(UnitOfMeasureController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public UnitOfMeasureController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    // ==================== Query Endpoints ====================

    @GetMapping
    @Operation(summary = "Get all units of measure", description = "Retrieves a list of all units of measure for the organization")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Units of measure retrieved successfully")
    })
    @PreAuthorize("hasAuthority('UOM_READ')")
    public ResponseEntity<List<UnitOfMeasureResponseDTO>> getAllUnitsOfMeasure() {
        UUID organizationId = TenantContext.current().organizationId();
        log.debug("Getting all units of measure for organization: {}", organizationId);
        List<UnitOfMeasureResponseDTO> result = queryBus.dispatch(new GetAllUnitsOfMeasureQuery(organizationId));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get unit of measure by ID", description = "Retrieves a unit of measure by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unit of measure retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Unit of measure not found")
    })
    @PreAuthorize("hasAuthority('UOM_READ')")
    public ResponseEntity<UnitOfMeasureResponseDTO> getUnitOfMeasureById(@PathVariable UUID id) {
        UUID organizationId = TenantContext.current().organizationId();
        log.debug("Getting unit of measure by ID: {}", id);
        try {
            UnitOfMeasureResponseDTO result = queryBus.dispatch(new GetUnitOfMeasureByIdQuery(id, organizationId));
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== Command Endpoints ====================

    @PostMapping
    @Operation(summary = "Create a new unit of measure", description = "Creates a new unit of measure")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Unit of measure created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Unit of measure with name/code already exists")
    })
    @PreAuthorize("hasAuthority('UOM_WRITE')")
    public ResponseEntity<UnitOfMeasureResponseDTO> createUnitOfMeasure(
            @Valid @RequestBody CreateUnitOfMeasureRequestDTO request
    ) {
        UUID organizationId = TenantContext.current().organizationId();
        log.info("Creating unit of measure: {} for organization: {}", request.name(), organizationId);
        try {
            CreateUnitOfMeasureCommand cmd = new CreateUnitOfMeasureCommand(
                    organizationId,
                    request.name(),
                    request.code()
            );
            UnitOfMeasureResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            log.warn("Unit of measure creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a unit of measure", description = "Updates an existing unit of measure")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unit of measure updated successfully"),
            @ApiResponse(responseCode = "404", description = "Unit of measure not found"),
            @ApiResponse(responseCode = "409", description = "Unit of measure with name/code already exists")
    })
    @PreAuthorize("hasAuthority('UOM_WRITE')")
    public ResponseEntity<UnitOfMeasureResponseDTO> updateUnitOfMeasure(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUnitOfMeasureRequestDTO request
    ) {
        UUID organizationId = TenantContext.current().organizationId();
        log.info("Updating unit of measure: {}", id);
        try {
            UpdateUnitOfMeasureCommand cmd = new UpdateUnitOfMeasureCommand(
                    id,
                    organizationId,
                    request.name(),
                    request.code()
            );
            UnitOfMeasureResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Unit of measure update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a unit of measure", description = "Soft deletes a unit of measure")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Unit of measure deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Unit of measure not found")
    })
    @PreAuthorize("hasAuthority('UOM_WRITE')")
    public ResponseEntity<Void> deleteUnitOfMeasure(@PathVariable UUID id) {
        UUID organizationId = TenantContext.current().organizationId();
        log.info("Deleting unit of measure: {}", id);
        try {
            commandBus.dispatch(new DeleteUnitOfMeasureCommand(id, organizationId));
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
