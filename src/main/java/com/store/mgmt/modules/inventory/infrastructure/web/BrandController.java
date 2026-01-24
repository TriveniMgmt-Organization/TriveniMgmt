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
 * REST controller for Brand management using Clean Architecture.
 */
@RestController
@RequestMapping("/api/v2/inventory/brands")
@Tag(name = "Brand Management (v2)", description = "Clean Architecture brand endpoints")
public class BrandController {

    private static final Logger log = LoggerFactory.getLogger(BrandController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public BrandController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    // ==================== Query Endpoints ====================

    @GetMapping
    @Operation(summary = "Get all brands", description = "Retrieves a list of all brands")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Brands retrieved successfully")
    })
    @PreAuthorize("hasAuthority('BRAND_READ')")
    public ResponseEntity<List<BrandResponseDTO>> getAllBrands(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    ) {
        log.debug("Getting all brands, includeInactive: {}", includeInactive);
        List<BrandResponseDTO> result = queryBus.dispatch(new GetAllBrandsQuery(includeInactive));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get brand by ID", description = "Retrieves a brand by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Brand retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Brand not found")
    })
    @PreAuthorize("hasAuthority('BRAND_READ')")
    public ResponseEntity<BrandResponseDTO> getBrandById(@PathVariable UUID id) {
        log.debug("Getting brand by ID: {}", id);
        try {
            BrandResponseDTO result = queryBus.dispatch(new GetBrandByIdQuery(id));
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== Command Endpoints ====================

    @PostMapping
    @Operation(summary = "Create a new brand", description = "Creates a new brand")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Brand created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Brand with name already exists")
    })
    @PreAuthorize("hasAuthority('BRAND_WRITE')")
    public ResponseEntity<BrandResponseDTO> createBrand(
            @Valid @RequestBody CreateBrandRequestDTO request
    ) {
        log.info("Creating brand: {}", request.name());
        try {
            CreateBrandCommand cmd = new CreateBrandCommand(
                    request.name(),
                    request.description(),
                    request.logoUrl(),
                    request.website(),
                    request.isActive()
            );
            BrandResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            log.warn("Brand creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a brand", description = "Updates an existing brand")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Brand updated successfully"),
            @ApiResponse(responseCode = "404", description = "Brand not found"),
            @ApiResponse(responseCode = "409", description = "Brand with name already exists")
    })
    @PreAuthorize("hasAuthority('BRAND_WRITE')")
    public ResponseEntity<BrandResponseDTO> updateBrand(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBrandRequestDTO request
    ) {
        log.info("Updating brand: {}", id);
        try {
            UpdateBrandCommand cmd = new UpdateBrandCommand(
                    id,
                    request.name(),
                    request.description(),
                    request.logoUrl(),
                    request.website(),
                    request.isActive()
            );
            BrandResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Brand update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a brand", description = "Soft deletes a brand")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Brand deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Brand not found")
    })
    @PreAuthorize("hasAuthority('BRAND_WRITE')")
    public ResponseEntity<Void> deleteBrand(@PathVariable UUID id) {
        log.info("Deleting brand: {}", id);
        try {
            commandBus.dispatch(new DeleteBrandCommand(id));
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
