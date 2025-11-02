package com.store.mgmt.globaltemplates.controller;

import com.store.mgmt.globaltemplates.model.dto.*;
import com.store.mgmt.globaltemplates.service.GlobalTemplateService;
import com.store.mgmt.globaltemplates.service.TemplateCopyService;
import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.repository.OrganizationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/global-templates")
@Tag(name = "GlobalTemplateController", description = "Operations related to global templates")
public class GlobalTemplateController {
    
    private final GlobalTemplateService templateService;
    private final TemplateCopyService templateCopyService;
    private final OrganizationRepository organizationRepository;
    
    public GlobalTemplateController(
            GlobalTemplateService templateService,
            TemplateCopyService templateCopyService,
            OrganizationRepository organizationRepository) {
        this.templateService = templateService;
        this.templateCopyService = templateCopyService;
        this.organizationRepository = organizationRepository;
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('TEMPLATE_WRITE')")
    @Operation(
            summary = "Create a new global template",
            description = "Creates a new global template that can be applied to organizations.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Template created successfully", content = @Content(schema = @Schema(implementation = GlobalTemplateDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "409", description = "Template with code already exists")
            }
    )
    public ResponseEntity<GlobalTemplateDTO> createTemplate(@Valid @RequestBody CreateGlobalTemplateDTO createDTO) {
        GlobalTemplateDTO template = templateService.createTemplate(createDTO);
        return new ResponseEntity<>(template, HttpStatus.CREATED);
    }
    
    @GetMapping
    @Operation(
            summary = "Get all global templates",
            description = "Retrieves a list of all global templates.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Templates retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GlobalTemplateDTO.class))))
            }
    )
    public ResponseEntity<List<GlobalTemplateDTO>> getAllTemplates() {
        List<GlobalTemplateDTO> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }
    
    @GetMapping("/active")
    @Operation(
            summary = "Get all active global templates",
            description = "Retrieves a list of all active global templates.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Active templates retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GlobalTemplateDTO.class))))
            }
    )
    public ResponseEntity<List<GlobalTemplateDTO>> getActiveTemplates() {
        List<GlobalTemplateDTO> templates = templateService.getActiveTemplates();
        return ResponseEntity.ok(templates);
    }
    
    @GetMapping("/type/{type}")
    @Operation(
            summary = "Get templates by type",
            description = "Retrieves templates filtered by type (RETAIL, GROCERY, PHARMA).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Templates retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GlobalTemplateDTO.class))))
            }
    )
    public ResponseEntity<List<GlobalTemplateDTO>> getTemplatesByType(
            @Parameter(description = "Template type", required = true) @PathVariable String type) {
        List<GlobalTemplateDTO> templates = templateService.getTemplatesByType(type);
        return ResponseEntity.ok(templates);
    }
    
    @GetMapping("/{id}")
    @Operation(
            summary = "Get global template by ID",
            description = "Retrieves a global template by its unique ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Template retrieved successfully", content = @Content(schema = @Schema(implementation = GlobalTemplateDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Template not found")
            }
    )
    public ResponseEntity<GlobalTemplateDTO> getTemplateById(@PathVariable UUID id) {
        GlobalTemplateDTO template = templateService.getTemplateById(id);
        return ResponseEntity.ok(template);
    }
    
    @GetMapping("/code/{code}")
    @Operation(
            summary = "Get global template by code",
            description = "Retrieves a global template by its unique code.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Template retrieved successfully", content = @Content(schema = @Schema(implementation = GlobalTemplateDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Template not found")
            }
    )
    public ResponseEntity<GlobalTemplateDTO> getTemplateByCode(@PathVariable String code) {
        GlobalTemplateDTO template = templateService.getTemplateByCode(code);
        return ResponseEntity.ok(template);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TEMPLATE_WRITE')")
    @Operation(
            summary = "Update a global template",
            description = "Updates an existing global template.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Template updated successfully", content = @Content(schema = @Schema(implementation = GlobalTemplateDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Template not found")
            }
    )
    public ResponseEntity<GlobalTemplateDTO> updateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGlobalTemplateDTO updateDTO) {
        GlobalTemplateDTO template = templateService.updateTemplate(id, updateDTO);
        return ResponseEntity.ok(template);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TEMPLATE_WRITE')")
    @Operation(
            summary = "Delete a global template",
            description = "Deletes a global template (logical delete).",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Template deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Template not found")
            }
    )
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/apply")
    @PreAuthorize("hasAuthority('TEMPLATE_WRITE')")
    @Operation(
            summary = "Apply a template to an organization",
            description = "Applies a global template to an organization, creating all entities defined in the template.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Template applied successfully"),
                    @ApiResponse(responseCode = "404", description = "Template or organization not found")
            }
    )
    public ResponseEntity<Void> applyTemplate(@Valid @RequestBody ApplyTemplateDTO applyDTO) {
        Organization org = organizationRepository.findById(applyDTO.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found: " + applyDTO.getOrganizationId()));
        
        templateCopyService.applyTemplate(org, applyDTO.getTemplateCode());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/items")
    @PreAuthorize("hasAuthority('TEMPLATE_WRITE')")
    @Operation(
            summary = "Add an item to a template",
            description = "Adds a new item (brand, category, UOM, etc.) to a global template.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Item added successfully", content = @Content(schema = @Schema(implementation = GlobalTemplateDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Template not found")
            }
    )
    public ResponseEntity<GlobalTemplateDTO> addItemToTemplate(
            @PathVariable UUID id,
            @RequestParam String entityType,
            @RequestParam String jsonData,
            @RequestParam(required = false, defaultValue = "0") Integer sortOrder) {
        GlobalTemplateDTO template = templateService.addItemToTemplate(id, entityType, jsonData, sortOrder);
        return ResponseEntity.ok(template);
    }
    
    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasAuthority('TEMPLATE_WRITE')")
    @Operation(
            summary = "Remove an item from a template",
            description = "Removes an item from a global template.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Item removed successfully"),
                    @ApiResponse(responseCode = "404", description = "Item not found")
            }
    )
    public ResponseEntity<Void> removeItemFromTemplate(@PathVariable UUID itemId) {
        templateService.removeItemFromTemplate(itemId);
        return ResponseEntity.noContent().build();
    }
}

