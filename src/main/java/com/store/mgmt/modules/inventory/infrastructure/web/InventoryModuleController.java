package com.store.mgmt.modules.inventory.infrastructure.web;

import com.store.mgmt.modules.inventory.application.command.*;
import com.store.mgmt.modules.inventory.application.dto.InventoryItemDTO;
import com.store.mgmt.modules.inventory.application.query.*;
import com.store.mgmt.shared.infrastructure.CommandBus;
import com.store.mgmt.shared.infrastructure.QueryBus;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
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
 * REST controller for Inventory module using Clean Architecture.
 * Uses Command/Query buses to dispatch to handlers.
 */
@RestController
@RequestMapping("/api/v2/inventory")
@Tag(name = "Inventory Module (v2)", description = "Clean Architecture inventory endpoints")
public class InventoryModuleController {

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public InventoryModuleController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    // ==================== Commands ====================

    @PostMapping("/items")
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_WRITE')")
    @Operation(summary = "Create inventory item", description = "Create a new inventory item for a product variant at a location")
    public ResponseEntity<InventoryItemDTO> createItem(
            @Valid @RequestBody CreateInventoryItemRequest request
    ) {
        UUID storeId = TenantContext.current().storeId();
        CreateInventoryItemCommand cmd = new CreateInventoryItemCommand(
                request.variantId(),
                request.locationId(),
                storeId,
                request.customBatchNumber(),
                request.expiryDate(),
                request.initialQuantity(),
                request.lowStockThreshold()
        );

        InventoryItemDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/items/{itemId}/receive")
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_WRITE')")
    @Operation(summary = "Receive stock", description = "Add stock to an inventory item")
    public ResponseEntity<InventoryItemDTO> receiveStock(
            @PathVariable UUID itemId,
            @Valid @RequestBody StockOperationRequest request
    ) {
        UUID storeId = TenantContext.current().storeId();
        ReceiveStockCommand cmd = new ReceiveStockCommand(
                itemId,
                storeId,
                request.quantity(),
                request.reason()
        );

        InventoryItemDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/items/{itemId}/issue")
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_WRITE')")
    @Operation(summary = "Issue stock", description = "Remove stock from an inventory item")
    public ResponseEntity<InventoryItemDTO> issueStock(
            @PathVariable UUID itemId,
            @Valid @RequestBody StockOperationRequest request
    ) {
        UUID storeId = TenantContext.current().storeId();
        IssueStockCommand cmd = new IssueStockCommand(
                itemId,
                storeId,
                request.quantity(),
                request.reason()
        );

        InventoryItemDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/items/{itemId}/adjust")
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_WRITE')")
    @Operation(summary = "Adjust stock", description = "Adjust stock level (positive or negative)")
    public ResponseEntity<InventoryItemDTO> adjustStock(
            @PathVariable UUID itemId,
            @Valid @RequestBody AdjustStockRequest request
    ) {
        UUID storeId = TenantContext.current().storeId();
        AdjustStockCommand cmd = new AdjustStockCommand(
                itemId,
                storeId,
                request.adjustment(),
                request.reason()
        );

        InventoryItemDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_WRITE')")
    @Operation(summary = "Delete inventory item", description = "Soft delete an inventory item")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID itemId) {
        UUID storeId = TenantContext.current().storeId();
        DeleteInventoryItemCommand cmd = new DeleteInventoryItemCommand(itemId, storeId);
        commandBus.dispatch(cmd);
        return ResponseEntity.noContent().build();
    }

    // ==================== Queries ====================

    @GetMapping("/items/{itemId}")
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_READ')")
    @Operation(summary = "Get inventory item", description = "Get a single inventory item by ID")
    public ResponseEntity<InventoryItemDTO> getItem(@PathVariable UUID itemId) {
        UUID storeId = TenantContext.current().storeId();
        GetInventoryItemQuery query = new GetInventoryItemQuery(itemId, storeId);
        InventoryItemDTO result = queryBus.dispatch(query);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/items")
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_READ')")
    @Operation(summary = "List inventory items", description = "Get inventory items with optional filters")
    public ResponseEntity<List<InventoryItemDTO>> getItems(
            @RequestParam(required = false, defaultValue = "false") boolean lowStockOnly,
            @RequestParam(required = false, defaultValue = "false") boolean expiringSoonOnly,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        UUID storeId = TenantContext.current().storeId();
        GetInventoryItemsQuery query = new GetInventoryItemsQuery(
                storeId,
                lowStockOnly,
                expiringSoonOnly,
                locationId,
                page,
                size
        );

        List<InventoryItemDTO> result = queryBus.dispatch(query);
        return ResponseEntity.ok(result);
    }

    // ==================== Request DTOs ====================

    public record CreateInventoryItemRequest(
            UUID variantId,
            UUID locationId,
            String customBatchNumber,
            java.time.LocalDate expiryDate,
            Integer initialQuantity,
            Integer lowStockThreshold
    ) {}

    public record StockOperationRequest(
            int quantity,
            String reason
    ) {}

    public record AdjustStockRequest(
            int adjustment,
            String reason
    ) {}
}
