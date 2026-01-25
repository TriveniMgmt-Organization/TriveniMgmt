package com.store.mgmt.modules.inventory.infrastructure.web;

import com.store.mgmt.modules.inventory.application.dto.*;
import com.store.mgmt.modules.inventory.application.query.*;
import com.store.mgmt.shared.infrastructure.QueryBus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Stock checking endpoints using Clean Architecture.
 */
@RestController
@RequestMapping("/api/v2/inventory/stock")
@Tag(name = "Stock Checks (v2)", description = "Clean Architecture stock checking endpoints")
public class StockCheckController {

    private static final Logger log = LoggerFactory.getLogger(StockCheckController.class);

    private final QueryBus queryBus;

    public StockCheckController(QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    @GetMapping("/variant/{variantId}")
    @Operation(summary = "Get stock summary by variant", description = "Retrieves total stock summary for a specific variant across all locations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock summary retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Variant not found")
    })
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_READ')")
    public ResponseEntity<StockSummaryResponseDTO> getStockByVariant(@PathVariable UUID variantId) {
        log.debug("Getting stock summary for variant: {}", variantId);
        try {
            StockSummaryResponseDTO result = queryBus.dispatch(new GetTotalStockByVariantQuery(variantId));
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/template/{templateId}")
    @Operation(summary = "Get stock summary by template", description = "Retrieves stock summary for all variants of a product template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock summaries retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_READ')")
    public ResponseEntity<List<StockSummaryResponseDTO>> getStockByTemplate(@PathVariable UUID templateId) {
        log.debug("Getting stock summary for template: {}", templateId);
        try {
            List<StockSummaryResponseDTO> result = queryBus.dispatch(new GetTotalStockByTemplateQuery(templateId));
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/availability/{variantId}")
    @Operation(summary = "Check stock availability", description = "Checks if the requested quantity is available for a variant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Availability check completed"),
            @ApiResponse(responseCode = "404", description = "Variant not found")
    })
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_READ')")
    public ResponseEntity<StockAvailabilityResponseDTO> checkAvailability(
            @PathVariable UUID variantId,
            @RequestParam int quantity
    ) {
        log.debug("Checking availability for variant: {}, quantity: {}", variantId, quantity);
        try {
            StockAvailabilityResponseDTO result = queryBus.dispatch(
                    new CheckStockAvailabilityQuery(variantId, quantity)
            );
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock items", description = "Retrieves all items that are below their low stock threshold")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Low stock items retrieved successfully")
    })
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_READ')")
    public ResponseEntity<List<LowStockItemResponseDTO>> getLowStockItems(
            @RequestParam(required = false) UUID locationId
    ) {
        log.debug("Getting low stock items, locationId: {}", locationId);
        List<LowStockItemResponseDTO> result = queryBus.dispatch(new GetLowStockItemsQuery(locationId));
        return ResponseEntity.ok(result);
    }
}
