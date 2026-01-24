package com.store.mgmt.modules.globaltemplates.infrastructure.web;

import com.store.mgmt.modules.globaltemplates.application.command.*;
import com.store.mgmt.modules.globaltemplates.application.dto.*;
import com.store.mgmt.modules.globaltemplates.application.query.*;
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
 * REST controller for Global Templates module using Clean Architecture.
 */
@RestController
@RequestMapping("/api/v2/global-templates")
@Tag(name = "Global Templates Module (v2)", description = "Clean Architecture global template endpoints")
public class GlobalTemplatesModuleController {

    private static final Logger log = LoggerFactory.getLogger(GlobalTemplatesModuleController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public GlobalTemplatesModuleController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    // ==================== Query Endpoints ====================

    @GetMapping
    @Operation(summary = "Get all global templates", description = "Retrieves a list of all global templates")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Templates retrieved successfully")
    })
    @PreAuthorize("hasAuthority('TEMPLATE_READ')")
    public ResponseEntity<List<GlobalTemplateResponseDTO>> getAllTemplates() {
        log.debug("Getting all templates");
        List<GlobalTemplateResponseDTO> result = queryBus.dispatch(new GetAllTemplatesQuery());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active global templates", description = "Retrieves a list of all active global templates")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active templates retrieved successfully")
    })
    @PreAuthorize("hasAuthority('TEMPLATE_READ')")
    public ResponseEntity<List<GlobalTemplateResponseDTO>> getActiveTemplates() {
        log.debug("Getting active templates");
        List<GlobalTemplateResponseDTO> result = queryBus.dispatch(new GetActiveTemplatesQuery());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get templates by type", description = "Retrieves templates filtered by type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Templates retrieved successfully")
    })
    @PreAuthorize("hasAuthority('TEMPLATE_READ')")
    public ResponseEntity<List<GlobalTemplateResponseDTO>> getTemplatesByType(@PathVariable String type) {
        log.debug("Getting templates by type: {}", type);
        List<GlobalTemplateResponseDTO> result = queryBus.dispatch(new GetTemplatesByTypeQuery(type));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get template by ID", description = "Retrieves a global template by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @PreAuthorize("hasAuthority('TEMPLATE_READ')")
    public ResponseEntity<GlobalTemplateResponseDTO> getTemplateById(@PathVariable UUID id) {
        log.debug("Getting template by ID: {}", id);
        try {
            GlobalTemplateResponseDTO result = queryBus.dispatch(new GetTemplateByIdQuery(id));
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get template by code", description = "Retrieves a global template by its unique code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @PreAuthorize("hasAuthority('TEMPLATE_READ')")
    public ResponseEntity<GlobalTemplateResponseDTO> getTemplateByCode(@PathVariable String code) {
        log.debug("Getting template by code: {}", code);
        try {
            GlobalTemplateResponseDTO result = queryBus.dispatch(new GetTemplateByCodeQuery(code));
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== Command Endpoints ====================

    @PostMapping
    @Operation(summary = "Create a new global template", description = "Creates a new global template")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Template created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Template with code already exists")
    })
    @PreAuthorize("hasAuthority('TEMPLATE_WRITE')")
    public ResponseEntity<GlobalTemplateResponseDTO> createTemplate(
            @Valid @RequestBody CreateTemplateRequestDTO request
    ) {
        log.info("Creating template: {}", request.getCode());
        try {
            CreateTemplateCommand cmd = new CreateTemplateCommand(
                    request.getName(),
                    request.getCode(),
                    request.getType(),
                    request.getDescription(),
                    request.getIsActive()
            );
            GlobalTemplateResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            log.warn("Template creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/from-json")
    @Operation(summary = "Create template from JSON", description = "Creates a global template from JSON data")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Template created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid JSON format"),
            @ApiResponse(responseCode = "409", description = "Template with code already exists")
    })
    @PreAuthorize("hasAuthority('TEMPLATE_WRITE')")
    public ResponseEntity<GlobalTemplateResponseDTO> createTemplateFromJson(
            @Valid @RequestBody CreateTemplateFromJsonRequestDTO request
    ) {
        log.info("Creating template from JSON");
        try {
            CreateTemplateFromJsonCommand cmd = new CreateTemplateFromJsonCommand(request.getJsonData());
            GlobalTemplateResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            log.warn("Template creation from JSON failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error creating template from JSON: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a global template", description = "Updates an existing global template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template updated successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @PreAuthorize("hasAuthority('TEMPLATE_WRITE')")
    public ResponseEntity<GlobalTemplateResponseDTO> updateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTemplateRequestDTO request
    ) {
        log.info("Updating template: {}", id);
        try {
            UpdateTemplateCommand cmd = new UpdateTemplateCommand(
                    id,
                    request.getName(),
                    request.getType(),
                    request.getDescription(),
                    request.getIsActive()
            );
            GlobalTemplateResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/from-json")
    @Operation(summary = "Update template from JSON", description = "Updates an existing template from JSON data")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid JSON format"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @PreAuthorize("hasAuthority('TEMPLATE_WRITE')")
    public ResponseEntity<GlobalTemplateResponseDTO> updateTemplateFromJson(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTemplateFromJsonRequestDTO request
    ) {
        log.info("Updating template from JSON: {}", id);
        try {
            UpdateTemplateFromJsonCommand cmd = new UpdateTemplateFromJsonCommand(id, request.getJsonData());
            GlobalTemplateResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating template from JSON: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a global template", description = "Soft deletes a global template")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Template deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @PreAuthorize("hasAuthority('TEMPLATE_WRITE')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID id) {
        log.info("Deleting template: {}", id);
        try {
            commandBus.dispatch(new DeleteTemplateCommand(id));
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/apply")
    @Operation(summary = "Apply template to organization", description = "Applies a global template to an organization")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template applied successfully"),
            @ApiResponse(responseCode = "404", description = "Template or organization not found")
    })
    @PreAuthorize("hasAuthority('TEMPLATE_WRITE')")
    public ResponseEntity<Void> applyTemplate(@Valid @RequestBody ApplyTemplateRequestDTO request) {
        log.info("Applying template '{}' to organization: {}", request.getTemplateCode(), request.getOrganizationId());
        try {
            ApplyTemplateCommand cmd = new ApplyTemplateCommand(
                    request.getOrganizationId(),
                    request.getTemplateCode()
            );
            commandBus.dispatch(cmd);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error applying template: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Add item to template", description = "Adds a new item to a global template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item added successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @PreAuthorize("hasAuthority('TEMPLATE_WRITE')")
    public ResponseEntity<GlobalTemplateResponseDTO> addItemToTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody AddTemplateItemRequestDTO request
    ) {
        log.info("Adding item to template: {} - type: {}", id, request.getEntityType());
        try {
            AddTemplateItemCommand cmd = new AddTemplateItemCommand(
                    id,
                    request.getEntityType(),
                    request.getJsonData(),
                    request.getSortOrder()
            );
            GlobalTemplateResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from template", description = "Removes an item from a global template")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item removed successfully"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @PreAuthorize("hasAuthority('TEMPLATE_WRITE')")
    public ResponseEntity<Void> removeItemFromTemplate(@PathVariable UUID itemId) {
        log.info("Removing item from template: {}", itemId);
        try {
            commandBus.dispatch(new RemoveTemplateItemCommand(itemId));
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
