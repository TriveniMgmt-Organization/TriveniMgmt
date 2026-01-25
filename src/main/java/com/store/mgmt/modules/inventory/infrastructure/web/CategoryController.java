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
 * REST controller for Category management using Clean Architecture.
 */
@RestController
@RequestMapping("/api/v2/inventory/categories")
@Tag(name = "Category Management (v2)", description = "Clean Architecture category endpoints")
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public CategoryController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    // ==================== Query Endpoints ====================

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieves a list of all categories for the organization")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    @PreAuthorize("hasAuthority('CATEGORY_READ')")
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    ) {
        UUID organizationId = TenantContext.current().organizationId();
        log.debug("Getting all categories for organization: {}, includeInactive: {}", organizationId, includeInactive);
        List<CategoryResponseDTO> result = queryBus.dispatch(new GetAllCategoriesQuery(organizationId, includeInactive));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves a category by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PreAuthorize("hasAuthority('CATEGORY_READ')")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable UUID id) {
        UUID organizationId = TenantContext.current().organizationId();
        log.debug("Getting category by ID: {}", id);
        try {
            CategoryResponseDTO result = queryBus.dispatch(new GetCategoryByIdQuery(id, organizationId));
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== Command Endpoints ====================

    @PostMapping
    @Operation(summary = "Create a new category", description = "Creates a new category")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Category with code/name already exists")
    })
    @PreAuthorize("hasAuthority('CATEGORY_WRITE')")
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CreateCategoryRequestDTO request
    ) {
        UUID organizationId = TenantContext.current().organizationId();
        log.info("Creating category: {} for organization: {}", request.name(), organizationId);
        try {
            CreateCategoryCommand cmd = new CreateCategoryCommand(
                    organizationId,
                    request.code(),
                    request.name(),
                    request.description(),
                    request.isActive()
            );
            CategoryResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            log.warn("Category creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a category", description = "Updates an existing category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Category with name already exists")
    })
    @PreAuthorize("hasAuthority('CATEGORY_WRITE')")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequestDTO request
    ) {
        UUID organizationId = TenantContext.current().organizationId();
        log.info("Updating category: {}", id);
        try {
            UpdateCategoryCommand cmd = new UpdateCategoryCommand(
                    id,
                    organizationId,
                    request.name(),
                    request.description(),
                    request.isActive()
            );
            CategoryResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Category update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category", description = "Soft deletes a category")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PreAuthorize("hasAuthority('CATEGORY_WRITE')")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        UUID organizationId = TenantContext.current().organizationId();
        log.info("Deleting category: {}", id);
        try {
            commandBus.dispatch(new DeleteCategoryCommand(id, organizationId));
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
