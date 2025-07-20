package com.store.mgmt.organization.controller;

import com.store.mgmt.organization.model.dto.CreateStoreDTO;
import com.store.mgmt.organization.model.dto.StoreDTO;
import com.store.mgmt.organization.model.entity.Store;
import com.store.mgmt.organization.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apiv1//stores")
public class StoreController {
    @Autowired
    private StoreService storeService;

    @PostMapping
    @PreAuthorize("hasAuthority('USER_WRITE')")
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
            @RequestBody CreateStoreDTO dto) {
        StoreDTO store = storeService.createStore(dto);
        return ResponseEntity.ok(store);
    }
}