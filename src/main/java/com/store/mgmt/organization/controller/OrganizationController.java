package com.store.mgmt.organization.controller;

import com.store.mgmt.organization.model.dto.CreateOrganizationDTO;
import com.store.mgmt.organization.model.dto.OrganizationDTO;
import com.store.mgmt.organization.model.dto.UpdateOrganizationDTO;
import com.store.mgmt.organization.service.OrganizationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {
    private final OrganizationServiceImpl organizationService;
    public OrganizationController(OrganizationServiceImpl organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    @Operation(
            summary = "Create a new organization",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Organization created successfully",
                            content =  @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationDTO.class))),
                            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
                            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content)
    }
    )
    public ResponseEntity<OrganizationDTO> createOrganization(
            @Parameter(description = "User details to be created", required = true)
            @RequestBody CreateOrganizationDTO request) {
        return ResponseEntity.ok(organizationService.createOrganization(request));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing organization",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Organization updated successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content)
            }
    )
    public ResponseEntity<OrganizationDTO> updateOrganization(
            @Parameter(description = "Unique ID of the organization to update", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Organization details to be updated", required = true)
            @RequestBody UpdateOrganizationDTO dto, HttpServletRequest httpRequest) throws IOException {
        String rawRequestBody = new BufferedReader(new InputStreamReader(httpRequest.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        System.out.println("Raw Request Body: " + rawRequestBody); // Check this output

        OrganizationDTO updatedOrganization = organizationService.updateOrganization(id, dto);
        return ResponseEntity.ok(updatedOrganization);
    }
}