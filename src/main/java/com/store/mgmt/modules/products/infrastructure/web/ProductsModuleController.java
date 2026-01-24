package com.store.mgmt.modules.products.infrastructure.web;

import com.store.mgmt.modules.products.application.command.*;
import com.store.mgmt.modules.products.application.dto.ProductTemplateDTO;
import com.store.mgmt.modules.products.application.dto.ProductVariantDTO;
import com.store.mgmt.modules.products.application.query.*;
import com.store.mgmt.shared.infrastructure.CommandBus;
import com.store.mgmt.shared.infrastructure.QueryBus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for Products module using Clean Architecture.
 * Uses Command/Query buses to dispatch to handlers.
 */
@RestController
@RequestMapping("/api/v2/products")
@Tag(name = "Products Module (v2)", description = "Clean Architecture product endpoints")
public class ProductsModuleController {

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public ProductsModuleController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    // ==================== Product Template Commands ====================

    @PostMapping("/templates")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    @Operation(summary = "Create product template", description = "Create a new product template")
    public ResponseEntity<ProductTemplateDTO> createTemplate(
            @Valid @RequestBody CreateProductTemplateRequest request
    ) {
        CreateProductTemplateCommand cmd = new CreateProductTemplateCommand(
                request.name(),
                request.description(),
                request.categoryId(),
                request.unitOfMeasureId(),
                request.brandId(),
                request.imageUrl(),
                request.reorderPoint(),
                request.requiresExpiry() != null && request.requiresExpiry(),
                request.attributes()
        );

        ProductTemplateDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/templates/{templateId}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    @Operation(summary = "Update product template", description = "Update an existing product template")
    public ResponseEntity<ProductTemplateDTO> updateTemplate(
            @PathVariable UUID templateId,
            @Valid @RequestBody UpdateProductTemplateRequest request
    ) {
        UpdateProductTemplateCommand cmd = new UpdateProductTemplateCommand(
                templateId,
                request.name(),
                request.description(),
                request.categoryId(),
                request.unitOfMeasureId(),
                request.brandId(),
                request.imageUrl(),
                request.reorderPoint(),
                request.requiresExpiry(),
                request.attributes()
        );

        ProductTemplateDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/templates/{templateId}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    @Operation(summary = "Delete product template", description = "Soft delete a product template")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID templateId) {
        DeleteProductTemplateCommand cmd = new DeleteProductTemplateCommand(templateId);
        commandBus.dispatch(cmd);
        return ResponseEntity.noContent().build();
    }

    // ==================== Product Template Queries ====================

    @GetMapping("/templates/{templateId}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(summary = "Get product template", description = "Get a single product template by ID")
    public ResponseEntity<ProductTemplateDTO> getTemplate(@PathVariable UUID templateId) {
        GetProductTemplateQuery query = new GetProductTemplateQuery(templateId);
        ProductTemplateDTO result = queryBus.dispatch(query);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/templates")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(summary = "List product templates", description = "Get product templates with optional filters")
    public ResponseEntity<List<ProductTemplateDTO>> getTemplates(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        GetProductTemplatesQuery query = new GetProductTemplatesQuery(categoryId, activeOnly, page, size);
        List<ProductTemplateDTO> result = queryBus.dispatch(query);
        return ResponseEntity.ok(result);
    }

    // ==================== Product Variant Commands ====================

    @PostMapping("/variants")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    @Operation(summary = "Create product variant", description = "Create a new product variant")
    public ResponseEntity<ProductVariantDTO> createVariant(
            @Valid @RequestBody CreateProductVariantRequest request
    ) {
        CreateProductVariantCommand cmd = new CreateProductVariantCommand(
                request.templateId(),
                request.sku(),
                request.barcode(),
                request.costPrice(),
                request.retailPrice(),
                request.attributeValues()
        );

        ProductVariantDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/variants/{variantId}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    @Operation(summary = "Update product variant", description = "Update an existing product variant")
    public ResponseEntity<ProductVariantDTO> updateVariant(
            @PathVariable UUID variantId,
            @Valid @RequestBody UpdateProductVariantRequest request
    ) {
        UpdateProductVariantCommand cmd = new UpdateProductVariantCommand(
                variantId,
                request.sku(),
                request.barcode(),
                request.costPrice(),
                request.retailPrice(),
                request.attributeValues(),
                request.active()
        );

        ProductVariantDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/variants/{variantId}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    @Operation(summary = "Delete product variant", description = "Soft delete a product variant")
    public ResponseEntity<Void> deleteVariant(@PathVariable UUID variantId) {
        DeleteProductVariantCommand cmd = new DeleteProductVariantCommand(variantId);
        commandBus.dispatch(cmd);
        return ResponseEntity.noContent().build();
    }

    // ==================== Product Variant Queries ====================

    @GetMapping("/variants/{variantId}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(summary = "Get product variant", description = "Get a single product variant by ID")
    public ResponseEntity<ProductVariantDTO> getVariant(@PathVariable UUID variantId) {
        GetProductVariantQuery query = new GetProductVariantQuery(variantId);
        ProductVariantDTO result = queryBus.dispatch(query);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/variants")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(summary = "List product variants", description = "Get product variants with optional filters")
    public ResponseEntity<List<ProductVariantDTO>> getVariants(
            @RequestParam(required = false) UUID templateId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        GetProductVariantsQuery query = new GetProductVariantsQuery(templateId, activeOnly, page, size);
        List<ProductVariantDTO> result = queryBus.dispatch(query);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/variants/search")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(summary = "Search product variant", description = "Search for a variant by SKU or barcode")
    public ResponseEntity<ProductVariantDTO> searchVariant(
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String barcode
    ) {
        SearchProductVariantsQuery query = new SearchProductVariantsQuery(sku, barcode);
        ProductVariantDTO result = queryBus.dispatch(query);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    // ==================== Request DTOs ====================

    public record CreateProductTemplateRequest(
            String name,
            String description,
            UUID categoryId,
            UUID unitOfMeasureId,
            UUID brandId,
            String imageUrl,
            Integer reorderPoint,
            Boolean requiresExpiry,
            Map<String, String> attributes
    ) {}

    public record UpdateProductTemplateRequest(
            String name,
            String description,
            UUID categoryId,
            UUID unitOfMeasureId,
            UUID brandId,
            String imageUrl,
            Integer reorderPoint,
            Boolean requiresExpiry,
            Map<String, String> attributes
    ) {}

    public record CreateProductVariantRequest(
            UUID templateId,
            String sku,
            String barcode,
            BigDecimal costPrice,
            BigDecimal retailPrice,
            Map<String, String> attributeValues
    ) {}

    public record UpdateProductVariantRequest(
            String sku,
            String barcode,
            BigDecimal costPrice,
            BigDecimal retailPrice,
            Map<String, String> attributeValues,
            Boolean active
    ) {}
}
