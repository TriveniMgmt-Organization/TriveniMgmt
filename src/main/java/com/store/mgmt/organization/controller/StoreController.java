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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreController {
    private final StoreService storeService;

    public  StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping
    @Operation(
            summary = "Create a new user",
            description = "Creates a new user account with the provided details.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User created successfully",
                            content = @Content(schema = @Schema(implementation = StoreDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input or missing required fields",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'CREATE_USER' authority",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict: User with provided email/username already exists",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<StoreDTO> createStore(
            @Parameter(description = "User details to be created", required = true)
            @Valid @RequestBody CreateStoreDTO dto, HttpServletRequest httpRequest) throws Exception {

        String rawRequestBody = new BufferedReader(new InputStreamReader(httpRequest.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        System.out.println("Raw Request Body: " + rawRequestBody); // Check this output
        StoreDTO store = storeService.createStore(dto);
        System.out.println("Store created: " + store); // Log the created store details
        return ResponseEntity.ok(store);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get store by ID",
            description = "Retrieves the details of a store by its unique ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Store retrieved successfully",
                            content = @Content(schema = @Schema(implementation = StoreDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not Found: Store with provided ID does not exist",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<StoreDTO> getStoreById(
            @Parameter(description = "Unique ID of the store to retrieve", required = true)
            @PathVariable UUID id) {
        StoreDTO store = storeService.getStoreById(id);
        return ResponseEntity.ok(store);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing store",
            description = "Updates the details of an existing store.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Store updated successfully",
                            content = @Content(schema = @Schema(implementation = StoreDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input or missing required fields",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'UPDATE_STORE' authority",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not Found: Store with provided ID does not exist",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<StoreDTO> updateStore(
            @Parameter(description = "Unique ID of the store to update", required = true)
            @PathVariable UUID id,
            @Parameter(description = "User details to be updated", required = true)
            @RequestBody UpdateStoreDTO dto) {
        StoreDTO store = storeService.updateStore(id, dto);
        return ResponseEntity.ok(store);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a store",
            description = "Deletes a store by its unique ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Store deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not Found: Store with provided ID does not exist",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<Void> deleteStore(
            @Parameter(description = "Unique ID of the store to delete", required = true)
            @PathVariable UUID id) {
        storeService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }
}