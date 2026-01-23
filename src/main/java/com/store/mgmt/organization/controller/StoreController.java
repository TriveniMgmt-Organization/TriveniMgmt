package com.store.mgmt.organization.controller;

import com.store.mgmt.organization.model.dto.CreateStoreDTO;
import com.store.mgmt.organization.model.dto.StoreDTO;
import com.store.mgmt.organization.model.dto.UpdateStoreDTO;
import com.store.mgmt.organization.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stores")
@Tag(name = "Stores", description = "Store management endpoints")
@Slf4j
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping
    @Operation(
            summary = "Create a new store",
            description = "Creates a new store within an organization.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Store created successfully",
                            content = @Content(schema = @Schema(implementation = StoreDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "409", description = "Store already exists")
            }
    )
    public ResponseEntity<StoreDTO> createStore(
            @Parameter(description = "Store details", required = true)
            @Valid @RequestBody CreateStoreDTO dto) {
        log.info("Creating store: {}", dto.getName());
        StoreDTO store = storeService.createStore(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(store);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get store by ID",
            description = "Retrieves the details of a store by its unique ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Store retrieved successfully",
                            content = @Content(schema = @Schema(implementation = StoreDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Store not found")
            }
    )
    public ResponseEntity<StoreDTO> getStoreById(
            @Parameter(description = "Store ID", required = true)
            @PathVariable UUID id) {
        log.debug("Fetching store with ID: {}", id);
        StoreDTO store = storeService.getStoreById(id);
        return ResponseEntity.ok(store);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a store",
            description = "Updates the details of an existing store.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Store updated successfully",
                            content = @Content(schema = @Schema(implementation = StoreDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Store not found")
            }
    )
    public ResponseEntity<StoreDTO> updateStore(
            @Parameter(description = "Store ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated store details", required = true)
            @Valid @RequestBody UpdateStoreDTO dto) {
        log.info("Updating store with ID: {}", id);
        StoreDTO store = storeService.updateStore(id, dto);
        return ResponseEntity.ok(store);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a store",
            description = "Deletes a store by its unique ID.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Store deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Store not found")
            }
    )
    public ResponseEntity<Void> deleteStore(
            @Parameter(description = "Store ID", required = true)
            @PathVariable UUID id) {
        log.info("Deleting store with ID: {}", id);
        storeService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }
}
