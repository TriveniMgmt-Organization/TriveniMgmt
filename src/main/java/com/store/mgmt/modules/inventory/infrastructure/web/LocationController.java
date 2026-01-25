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
 * REST controller for Inventory Location management using Clean Architecture.
 */
@RestController
@RequestMapping("/api/v2/inventory/locations")
@Tag(name = "Location Management (v2)", description = "Clean Architecture inventory location endpoints")
public class LocationController {

    private static final Logger log = LoggerFactory.getLogger(LocationController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public LocationController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    // ==================== Query Endpoints ====================

    @GetMapping
    @Operation(summary = "Get all locations", description = "Retrieves a list of all inventory locations for the store")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locations retrieved successfully")
    })
    @PreAuthorize("hasAuthority('LOCATION_READ')")
    public ResponseEntity<List<LocationResponseDTO>> getAllLocations(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    ) {
        UUID storeId = TenantContext.current().storeId();
        log.debug("Getting all locations for store: {}, includeInactive: {}", storeId, includeInactive);
        List<LocationResponseDTO> result = queryBus.dispatch(new GetAllLocationsQuery(storeId, includeInactive));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get location by ID", description = "Retrieves a location by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Location not found")
    })
    @PreAuthorize("hasAuthority('LOCATION_READ')")
    public ResponseEntity<LocationResponseDTO> getLocationById(@PathVariable UUID id) {
        UUID storeId = TenantContext.current().storeId();
        log.debug("Getting location by ID: {}", id);
        try {
            LocationResponseDTO result = queryBus.dispatch(new GetLocationByIdQuery(id, storeId));
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== Command Endpoints ====================

    @PostMapping
    @Operation(summary = "Create a new location", description = "Creates a new inventory location")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Location created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Location with name already exists")
    })
    @PreAuthorize("hasAuthority('LOCATION_WRITE')")
    public ResponseEntity<LocationResponseDTO> createLocation(
            @Valid @RequestBody CreateLocationRequestDTO request
    ) {
        UUID storeId = TenantContext.current().storeId();
        log.info("Creating location: {} for store: {}", request.name(), storeId);
        try {
            CreateLocationCommand cmd = new CreateLocationCommand(
                    storeId,
                    request.name(),
                    request.address(),
                    request.type(),
                    request.isActive()
            );
            LocationResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            log.warn("Location creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a location", description = "Updates an existing inventory location")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location updated successfully"),
            @ApiResponse(responseCode = "404", description = "Location not found"),
            @ApiResponse(responseCode = "409", description = "Location with name already exists")
    })
    @PreAuthorize("hasAuthority('LOCATION_WRITE')")
    public ResponseEntity<LocationResponseDTO> updateLocation(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLocationRequestDTO request
    ) {
        UUID storeId = TenantContext.current().storeId();
        log.info("Updating location: {}", id);
        try {
            UpdateLocationCommand cmd = new UpdateLocationCommand(
                    id,
                    storeId,
                    request.name(),
                    request.address(),
                    request.type(),
                    request.isActive()
            );
            LocationResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Location update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a location", description = "Soft deletes an inventory location")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Location deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Location not found")
    })
    @PreAuthorize("hasAuthority('LOCATION_WRITE')")
    public ResponseEntity<Void> deleteLocation(@PathVariable UUID id) {
        UUID storeId = TenantContext.current().storeId();
        log.info("Deleting location: {}", id);
        try {
            commandBus.dispatch(new DeleteLocationCommand(id, storeId));
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
