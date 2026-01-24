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
 * REST controller for Purchase Order management using Clean Architecture.
 */
@RestController
@RequestMapping("/api/v2/inventory/purchase-orders")
@Tag(name = "Purchase Orders (v2)", description = "Clean Architecture purchase order endpoints")
public class PurchaseOrderController {

    private static final Logger log = LoggerFactory.getLogger(PurchaseOrderController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public PurchaseOrderController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    // ==================== Query Endpoints ====================

    @GetMapping
    @Operation(summary = "Get all purchase orders", description = "Retrieves all purchase orders for the organization")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Purchase orders retrieved successfully")
    })
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_READ')")
    public ResponseEntity<List<PurchaseOrderResponseDTO>> getAllPurchaseOrders(
            @RequestHeader("X-Organization-Id") UUID organizationId
    ) {
        log.debug("Getting all purchase orders for organization: {}", organizationId);
        List<PurchaseOrderResponseDTO> result = queryBus.dispatch(new GetAllPurchaseOrdersQuery(organizationId));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase order by ID", description = "Retrieves a purchase order by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Purchase order retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Purchase order not found")
    })
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_READ')")
    public ResponseEntity<PurchaseOrderResponseDTO> getPurchaseOrderById(
            @PathVariable UUID id,
            @RequestHeader("X-Organization-Id") UUID organizationId
    ) {
        log.debug("Getting purchase order by ID: {}", id);
        try {
            PurchaseOrderResponseDTO result = queryBus.dispatch(new GetPurchaseOrderByIdQuery(id, organizationId));
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get purchase orders by status", description = "Retrieves purchase orders by status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Purchase orders retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status")
    })
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_READ')")
    public ResponseEntity<List<PurchaseOrderResponseDTO>> getPurchaseOrdersByStatus(
            @PathVariable String status,
            @RequestHeader("X-Organization-Id") UUID organizationId
    ) {
        log.debug("Getting purchase orders by status: {}", status);
        try {
            List<PurchaseOrderResponseDTO> result = queryBus.dispatch(
                    new GetPurchaseOrdersByStatusQuery(organizationId, status)
            );
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== Command Endpoints ====================

    @PostMapping
    @Operation(summary = "Create a new purchase order", description = "Creates a new purchase order")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Purchase order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Related entity not found")
    })
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_WRITE')")
    public ResponseEntity<PurchaseOrderResponseDTO> createPurchaseOrder(
            @RequestHeader("X-Organization-Id") UUID organizationId,
            @Valid @RequestBody CreatePurchaseOrderRequestDTO request
    ) {
        log.info("Creating purchase order for supplier: {}", request.supplierId());
        try {
            CreatePurchaseOrderCommand cmd = new CreatePurchaseOrderCommand(
                    organizationId,
                    request.supplierId(),
                    request.expectedDeliveryDate(),
                    request.trackingNumber(),
                    request.notes(),
                    request.items(),
                    null // userId - could be extracted from authentication
            );
            PurchaseOrderResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Purchase order creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a purchase order", description = "Updates an existing purchase order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Purchase order updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or state"),
            @ApiResponse(responseCode = "404", description = "Purchase order not found")
    })
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_WRITE')")
    public ResponseEntity<PurchaseOrderResponseDTO> updatePurchaseOrder(
            @PathVariable UUID id,
            @RequestHeader("X-Organization-Id") UUID organizationId,
            @Valid @RequestBody UpdatePurchaseOrderRequestDTO request
    ) {
        log.info("Updating purchase order: {}", id);
        try {
            UpdatePurchaseOrderCommand cmd = new UpdatePurchaseOrderCommand(
                    id,
                    organizationId,
                    request.expectedDeliveryDate(),
                    request.trackingNumber(),
                    request.notes(),
                    request.status()
            );
            PurchaseOrderResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Purchase order update failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/receive")
    @Operation(summary = "Receive items for a purchase order", description = "Records receipt of items for a purchase order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Items received successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or state"),
            @ApiResponse(responseCode = "404", description = "Purchase order or item not found")
    })
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_WRITE')")
    public ResponseEntity<PurchaseOrderResponseDTO> receivePurchaseOrder(
            @PathVariable UUID id,
            @RequestHeader("X-Organization-Id") UUID organizationId,
            @Valid @RequestBody ReceivePurchaseOrderRequestDTO request
    ) {
        log.info("Receiving items for purchase order: {}", id);
        try {
            ReceivePurchaseOrderCommand cmd = new ReceivePurchaseOrderCommand(
                    id,
                    organizationId,
                    request.items(),
                    null // userId - could be extracted from authentication
            );
            PurchaseOrderResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Purchase order receipt failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a purchase order", description = "Cancels a purchase order")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Purchase order cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot cancel purchase order"),
            @ApiResponse(responseCode = "404", description = "Purchase order not found")
    })
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_WRITE')")
    public ResponseEntity<Void> cancelPurchaseOrder(
            @PathVariable UUID id,
            @RequestHeader("X-Organization-Id") UUID organizationId
    ) {
        log.info("Cancelling purchase order: {}", id);
        try {
            commandBus.dispatch(new CancelPurchaseOrderCommand(id, organizationId));
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Purchase order cancellation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
