package com.store.mgmt.modules.organization.infrastructure.web;

import com.store.mgmt.modules.organization.application.command.*;
import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.modules.organization.application.query.*;
import com.store.mgmt.shared.infrastructure.CommandBus;
import com.store.mgmt.shared.infrastructure.QueryBus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Organization module using Clean Architecture.
 * Uses Command/Query buses to dispatch to handlers.
 */
@RestController
@RequestMapping("/api/v2/organizations")
@Tag(name = "Organizations Module (v2)", description = "Clean Architecture organization endpoints")
public class OrganizationModuleController {

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public OrganizationModuleController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    // ==================== Organization Commands ====================

    @PostMapping
    @PreAuthorize("hasAuthority('ORG_WRITE')")
    @Operation(summary = "Create organization", description = "Create a new organization")
    public ResponseEntity<OrganizationDTO> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request
    ) {
        CreateOrganizationCommand cmd = new CreateOrganizationCommand(
                request.name(),
                request.description(),
                request.contactInfo(),
                request.templateCode()
        );

        OrganizationDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{organizationId}")
    @PreAuthorize("hasAuthority('ORG_WRITE')")
    @Operation(summary = "Update organization", description = "Update an existing organization")
    public ResponseEntity<OrganizationDTO> updateOrganization(
            @PathVariable UUID organizationId,
            @Valid @RequestBody UpdateOrganizationRequest request
    ) {
        UpdateOrganizationCommand cmd = new UpdateOrganizationCommand(
                organizationId,
                request.name(),
                request.description(),
                request.contactInfo()
        );

        OrganizationDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{organizationId}")
    @PreAuthorize("hasAuthority('ORG_DELETE')")
    @Operation(summary = "Delete organization", description = "Soft delete an organization")
    public ResponseEntity<Void> deleteOrganization(@PathVariable UUID organizationId) {
        DeleteOrganizationCommand cmd = new DeleteOrganizationCommand(organizationId);
        commandBus.dispatch(cmd);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{organizationId}/apply-template")
    @PreAuthorize("hasAuthority('ORG_WRITE')")
    @Operation(summary = "Apply template", description = "Apply a template to the organization (one-time operation)")
    public ResponseEntity<OrganizationDTO> applyTemplate(
            @PathVariable UUID organizationId,
            @Valid @RequestBody ApplyTemplateRequest request
    ) {
        ApplyTemplateCommand cmd = new ApplyTemplateCommand(organizationId, request.templateCode());
        commandBus.dispatch(cmd);

        // Query for the updated organization
        GetOrganizationQuery query = new GetOrganizationQuery(organizationId);
        OrganizationDTO result = queryBus.dispatch(query);
        return ResponseEntity.ok(result);
    }

    // ==================== Organization Queries ====================

    @GetMapping("/{organizationId}")
    @PreAuthorize("hasAuthority('ORG_READ')")
    @Operation(summary = "Get organization", description = "Get a single organization by ID with its stores")
    public ResponseEntity<OrganizationDTO> getOrganization(@PathVariable UUID organizationId) {
        GetOrganizationQuery query = new GetOrganizationQuery(organizationId);
        OrganizationDTO result = queryBus.dispatch(query);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ORG_READ')")
    @Operation(summary = "List organizations", description = "Get organizations for the current user")
    public ResponseEntity<List<OrganizationDTO>> getOrganizations(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        GetOrganizationsQuery query = new GetOrganizationsQuery(page, size);
        List<OrganizationDTO> result = queryBus.dispatch(query);
        return ResponseEntity.ok(result);
    }

    // ==================== Store Commands ====================

    @PostMapping("/{organizationId}/stores")
    @PreAuthorize("hasAuthority('STORE_WRITE')")
    @Operation(summary = "Create store", description = "Create a new store within an organization")
    public ResponseEntity<StoreDTO> createStore(
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateStoreRequest request
    ) {
        CreateStoreCommand cmd = new CreateStoreCommand(
                organizationId,
                request.name(),
                request.location(),
                request.countryCode(),
                request.contactInfo()
        );

        StoreDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{organizationId}/stores/{storeId}")
    @PreAuthorize("hasAuthority('STORE_WRITE')")
    @Operation(summary = "Update store", description = "Update an existing store")
    public ResponseEntity<StoreDTO> updateStore(
            @PathVariable UUID organizationId,
            @PathVariable UUID storeId,
            @Valid @RequestBody UpdateStoreRequest request
    ) {
        UpdateStoreCommand cmd = new UpdateStoreCommand(
                storeId,
                request.name(),
                request.location(),
                request.countryCode(),
                request.contactInfo(),
                request.status()
        );

        StoreDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{organizationId}/stores/{storeId}")
    @PreAuthorize("hasAuthority('STORE_DELETE')")
    @Operation(summary = "Delete store", description = "Soft delete a store")
    public ResponseEntity<Void> deleteStore(
            @PathVariable UUID organizationId,
            @PathVariable UUID storeId
    ) {
        DeleteStoreCommand cmd = new DeleteStoreCommand(storeId);
        commandBus.dispatch(cmd);
        return ResponseEntity.noContent().build();
    }

    // ==================== Store Queries ====================

    @GetMapping("/{organizationId}/stores/{storeId}")
    @PreAuthorize("hasAuthority('STORE_READ')")
    @Operation(summary = "Get store", description = "Get a single store by ID")
    public ResponseEntity<StoreDTO> getStore(
            @PathVariable UUID organizationId,
            @PathVariable UUID storeId
    ) {
        GetStoreQuery query = new GetStoreQuery(storeId);
        StoreDTO result = queryBus.dispatch(query);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{organizationId}/stores")
    @PreAuthorize("hasAuthority('STORE_READ')")
    @Operation(summary = "List stores", description = "Get stores for an organization")
    public ResponseEntity<List<StoreDTO>> getStores(
            @PathVariable UUID organizationId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        GetStoresQuery query = new GetStoresQuery(organizationId, page, size);
        List<StoreDTO> result = queryBus.dispatch(query);
        return ResponseEntity.ok(result);
    }

    // ==================== Request DTOs ====================

    public record CreateOrganizationRequest(
            String name,
            String description,
            String contactInfo,
            String templateCode
    ) {}

    public record UpdateOrganizationRequest(
            String name,
            String description,
            String contactInfo
    ) {}

    public record ApplyTemplateRequest(
            String templateCode
    ) {}

    public record CreateStoreRequest(
            String name,
            String location,
            String countryCode,
            String contactInfo
    ) {}

    public record UpdateStoreRequest(
            String name,
            String location,
            String countryCode,
            String contactInfo,
            String status
    ) {}
}
