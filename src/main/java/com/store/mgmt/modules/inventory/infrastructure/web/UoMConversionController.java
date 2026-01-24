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
 * REST controller for UoM Conversion management using Clean Architecture.
 */
@RestController
@RequestMapping("/api/v2/inventory/uom-conversions")
@Tag(name = "UoM Conversions (v2)", description = "Clean Architecture UoM conversion endpoints")
public class UoMConversionController {

    private static final Logger log = LoggerFactory.getLogger(UoMConversionController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public UoMConversionController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    // ==================== Query Endpoints ====================

    @GetMapping
    @Operation(summary = "Get all UoM conversions", description = "Retrieves a list of all UoM conversions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "UoM conversions retrieved successfully")
    })
    @PreAuthorize("hasAuthority('UNIT_OF_MEASURE_READ')")
    public ResponseEntity<List<UoMConversionResponseDTO>> getAllConversions() {
        log.debug("Getting all UoM conversions");
        List<UoMConversionResponseDTO> result = queryBus.dispatch(new GetAllUoMConversionsQuery());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get UoM conversion by ID", description = "Retrieves a UoM conversion by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "UoM conversion retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "UoM conversion not found")
    })
    @PreAuthorize("hasAuthority('UNIT_OF_MEASURE_READ')")
    public ResponseEntity<UoMConversionResponseDTO> getConversionById(@PathVariable UUID id) {
        log.debug("Getting UoM conversion by ID: {}", id);
        try {
            UoMConversionResponseDTO result = queryBus.dispatch(new GetUoMConversionByIdQuery(id));
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/between")
    @Operation(summary = "Get conversion between two UoMs", description = "Retrieves the conversion ratio between two specific UoMs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversion found"),
            @ApiResponse(responseCode = "404", description = "No conversion found between the specified UoMs")
    })
    @PreAuthorize("hasAuthority('UNIT_OF_MEASURE_READ')")
    public ResponseEntity<UoMConversionResponseDTO> getConversionBetween(
            @RequestParam UUID fromUomId,
            @RequestParam UUID toUomId
    ) {
        log.debug("Getting conversion from {} to {}", fromUomId, toUomId);
        try {
            UoMConversionResponseDTO result = queryBus.dispatch(
                    new GetConversionBetweenUomsQuery(fromUomId, toUomId)
            );
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== Command Endpoints ====================

    @PostMapping
    @Operation(summary = "Create a new UoM conversion", description = "Creates a new UoM conversion")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "UoM conversion created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "UoM not found"),
            @ApiResponse(responseCode = "409", description = "Conversion already exists")
    })
    @PreAuthorize("hasAuthority('UNIT_OF_MEASURE_WRITE')")
    public ResponseEntity<UoMConversionResponseDTO> createConversion(
            @Valid @RequestBody CreateUoMConversionRequestDTO request
    ) {
        log.info("Creating UoM conversion from {} to {}", request.fromUomId(), request.toUomId());
        try {
            CreateUoMConversionCommand cmd = new CreateUoMConversionCommand(
                    request.fromUomId(),
                    request.toUomId(),
                    request.ratio()
            );
            UoMConversionResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("UoM conversion creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a UoM conversion", description = "Soft deletes a UoM conversion")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "UoM conversion deleted successfully"),
            @ApiResponse(responseCode = "404", description = "UoM conversion not found")
    })
    @PreAuthorize("hasAuthority('UNIT_OF_MEASURE_WRITE')")
    public ResponseEntity<Void> deleteConversion(@PathVariable UUID id) {
        log.info("Deleting UoM conversion: {}", id);
        try {
            commandBus.dispatch(new DeleteUoMConversionCommand(id));
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
