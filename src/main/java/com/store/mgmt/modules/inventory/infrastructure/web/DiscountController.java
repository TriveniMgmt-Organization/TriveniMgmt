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
 * REST controller for Discount management using Clean Architecture.
 */
@RestController
@RequestMapping("/api/v2/inventory/discounts")
@Tag(name = "Discount Management (v2)", description = "Clean Architecture discount endpoints")
public class DiscountController {

    private static final Logger log = LoggerFactory.getLogger(DiscountController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public DiscountController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    // ==================== Query Endpoints ====================

    @GetMapping
    @Operation(summary = "Get all discounts", description = "Retrieves all discounts for the organization")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Discounts retrieved successfully")
    })
    @PreAuthorize("hasAuthority('DISCOUNT_READ')")
    public ResponseEntity<List<DiscountResponseDTO>> getAllDiscounts(
            @RequestHeader("X-Organization-Id") UUID organizationId,
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    ) {
        log.debug("Getting all discounts for organization: {}, includeInactive: {}", organizationId, includeInactive);
        List<DiscountResponseDTO> result = queryBus.dispatch(new GetAllDiscountsQuery(organizationId, includeInactive));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get discount by ID", description = "Retrieves a discount by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Discount retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Discount not found")
    })
    @PreAuthorize("hasAuthority('DISCOUNT_READ')")
    public ResponseEntity<DiscountResponseDTO> getDiscountById(
            @PathVariable UUID id,
            @RequestHeader("X-Store-Id") UUID storeId
    ) {
        log.debug("Getting discount by ID: {}", id);
        try {
            DiscountResponseDTO result = queryBus.dispatch(new GetDiscountByIdQuery(id, storeId));
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/product/{productTemplateId}/active")
    @Operation(summary = "Get active discounts for product", description = "Retrieves all active discounts applicable to a product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Discounts retrieved successfully")
    })
    @PreAuthorize("hasAuthority('DISCOUNT_READ')")
    public ResponseEntity<List<DiscountResponseDTO>> getActiveDiscountsForProduct(@PathVariable UUID productTemplateId) {
        log.debug("Getting active discounts for product: {}", productTemplateId);
        List<DiscountResponseDTO> result = queryBus.dispatch(new GetActiveDiscountsForProductQuery(productTemplateId));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/category/{categoryId}/active")
    @Operation(summary = "Get active discounts for category", description = "Retrieves all active discounts applicable to a category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Discounts retrieved successfully")
    })
    @PreAuthorize("hasAuthority('DISCOUNT_READ')")
    public ResponseEntity<List<DiscountResponseDTO>> getActiveDiscountsForCategory(@PathVariable UUID categoryId) {
        log.debug("Getting active discounts for category: {}", categoryId);
        List<DiscountResponseDTO> result = queryBus.dispatch(new GetActiveDiscountsForCategoryQuery(categoryId));
        return ResponseEntity.ok(result);
    }

    // ==================== Command Endpoints ====================

    @PostMapping
    @Operation(summary = "Create a new discount", description = "Creates a new discount")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Discount created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Related entity not found"),
            @ApiResponse(responseCode = "409", description = "Discount name already exists")
    })
    @PreAuthorize("hasAuthority('DISCOUNT_WRITE')")
    public ResponseEntity<DiscountResponseDTO> createDiscount(
            @RequestHeader("X-Organization-Id") UUID organizationId,
            @RequestHeader("X-Store-Id") UUID storeId,
            @Valid @RequestBody CreateDiscountRequestDTO request
    ) {
        log.info("Creating discount: {}", request.name());
        try {
            CreateDiscountCommand cmd = new CreateDiscountCommand(
                    organizationId,
                    storeId,
                    request.name(),
                    request.type(),
                    request.value(),
                    request.startDate(),
                    request.endDate(),
                    request.productTemplateId(),
                    request.categoryId(),
                    request.description(),
                    request.minimumPurchaseAmount(),
                    request.minimumItemQuantity(),
                    request.isActive()
            );
            DiscountResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Discount creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a discount", description = "Updates an existing discount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Discount updated successfully"),
            @ApiResponse(responseCode = "404", description = "Discount not found"),
            @ApiResponse(responseCode = "409", description = "Discount name already exists")
    })
    @PreAuthorize("hasAuthority('DISCOUNT_WRITE')")
    public ResponseEntity<DiscountResponseDTO> updateDiscount(
            @PathVariable UUID id,
            @RequestHeader("X-Store-Id") UUID storeId,
            @Valid @RequestBody UpdateDiscountRequestDTO request
    ) {
        log.info("Updating discount: {}", id);
        try {
            UpdateDiscountCommand cmd = new UpdateDiscountCommand(
                    id,
                    storeId,
                    request.name(),
                    request.type(),
                    request.value(),
                    request.startDate(),
                    request.endDate(),
                    request.productTemplateId(),
                    request.categoryId(),
                    request.description(),
                    request.minimumPurchaseAmount(),
                    request.minimumItemQuantity(),
                    request.isActive()
            );
            DiscountResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Discount update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a discount", description = "Deactivates a discount")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Discount deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Discount not found")
    })
    @PreAuthorize("hasAuthority('DISCOUNT_WRITE')")
    public ResponseEntity<Void> deactivateDiscount(
            @PathVariable UUID id,
            @RequestHeader("X-Store-Id") UUID storeId
    ) {
        log.info("Deactivating discount: {}", id);
        try {
            commandBus.dispatch(new DeactivateDiscountCommand(id, storeId));
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
