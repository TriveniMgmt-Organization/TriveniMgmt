package com.store.mgmt.inventory.controller;

import com.store.mgmt.inventory.model.dto.*;
import com.store.mgmt.inventory.model.entity.PurchaseOrder;
import com.store.mgmt.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid; // Import for @Valid

import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/v1/inventory") // Base path for all inventory related operations
@Tag(name = "Inventory", description = "Comprehensive API for managing product inventory, sales, purchases, and related master data.")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService){
        this.inventoryService = inventoryService;
    }

    // --- Inventory Item Management (Specific stock items) ---

    @PostMapping("/items")
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_WRITE')")
    @Operation(
            summary = "Create or update an inventory item",
            description = "Adds a new inventory item record for a product at a specific location, batch, and expiration. If an identical item already exists, its quantity will be updated.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "New inventory item created successfully",
                            content = @Content(schema = @Schema(implementation = InventoryItemDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "200",
                            description = "Existing inventory item quantity updated successfully",
                            content = @Content(schema = @Schema(implementation = InventoryItemDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input or missing required fields",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Product or Location not found",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'PERM_CREATE_INVENTORY_ITEM' authority",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<InventoryItemDTO> createInventoryItem(
            @Parameter(description = "Details of the inventory item to be created or updated", required = true)
            @Valid @RequestBody CreateInventoryItemDTO request) {
        // The service layer handles whether this is a new creation or an update to an existing item's quantity.
        InventoryItemDTO newOrUpdatedInventoryItem = inventoryService.createInventoryItem(request);
        // If an item was truly created (vs updated), 201 is more appropriate.
        // For simplicity, we'll return 200/201 based on service's internal logic which is not directly exposed here.
        // A more complex controller might check the DTO's ID to determine if it's new or existing.
        return new ResponseEntity<>(newOrUpdatedInventoryItem, HttpStatus.OK); // Or HttpStatus.CREATED if truly new
    }

    @GetMapping("/items/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_READ')")
    @Operation(
            summary = "Get an inventory item by ID",
            description = "Retrieves a single inventory item record based on its unique identifier.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Inventory item retrieved successfully",
                            content = @Content(schema = @Schema(implementation = InventoryItemDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Inventory item not found",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'PERM_VIEW_INVENTORY_ITEM' authority",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<InventoryItemDTO> getInventoryItem(
            @Parameter(description = "Unique ID of the inventory item record to retrieve", required = true)
            @PathVariable UUID id) { // Changed UUID to UUID
        InventoryItemDTO inventoryItem = inventoryService.getInventoryItemById(id);
        return ResponseEntity.ok(inventoryItem);
    }

    @GetMapping("/items/by-product/{productTemplateId}")
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_READ')")
    @Operation(
            summary = "Get inventory items for a specific product",
            description = "Retrieves a list of all inventory item records for a given product across all locations.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of inventory items retrieved successfully",
                            content = @Content(array = @ArraySchema(schema = @Schema(required = true, implementation = InventoryItemDTO.class)))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Product not found",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'PERM_VIEW_INVENTORY_ITEM' authority",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<List<InventoryItemDTO>> getInventoryItemsByProduct(
            @Parameter(description = "ID of the product to retrieve inventory items for", required = true)
            @PathVariable UUID productTemplateId) {
        List<InventoryItemDTO> inventoryItems = inventoryService.getInventoryItemsForProduct(productTemplateId);
        return ResponseEntity.ok(inventoryItems);
    }

    @GetMapping("/items/by-location/{locationId}")
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_READ')")
    @Operation(
            summary = "Get inventory items at a specific location",
            description = "Retrieves a list of all inventory item records currently stored at a given location.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of inventory items retrieved successfully",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = InventoryItemDTO.class)))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Location not found",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'PERM_VIEW_INVENTORY_ITEM' authority",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<List<InventoryItemDTO>> getInventoryItemsAtLocation(
            @Parameter(description = "ID of the location to retrieve inventory items from", required = true)
            @PathVariable UUID locationId) {
        List<InventoryItemDTO> inventoryItems = inventoryService.getInventoryItemsAtLocation(locationId);
        return ResponseEntity.ok(inventoryItems);
    }


    @PatchMapping("/items/{id}/quantity") // Using PATCH for partial update
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_WRITE')")
    @Operation(
            summary = "Update the quantity of an inventory item",
            description = "Adjusts the quantity of an existing inventory item identified by its ID. Use positive values to add, negative to subtract.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Inventory item quantity updated successfully",
                            content = @Content(schema = @Schema(implementation = InventoryItemDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input (e.g., resulting quantity would be negative)",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Inventory item not found",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'PERM_UPDATE_INVENTORY_ITEM_QUANTITY' authority",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<InventoryItemDTO> updateInventoryItemQuantity(
            @Parameter(description = "Unique ID of the inventory item to update", required = true)
            @PathVariable UUID id, // Changed UUID to UUID
            @Parameter(description = "Quantity change (positive for add, negative for subtract)", required = true)
            @Valid @RequestBody UpdateInventoryItemDTO request) { // New DTO for quantity change
        InventoryItemDTO updatedInventory = inventoryService.updateInventoryItemQuantity(id, request.getQuantityChange());
        return ResponseEntity.ok(updatedInventory);
    }

    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_ITEM_WRITE')")
    @Operation(
            summary = "Delete an inventory item",
            description = "Deletes an inventory item record based on its unique identifier. This might represent removing a specific batch/lot from stock.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Inventory item deleted successfully",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Inventory item not found",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'PERM_DELETE_INVENTORY_ITEM' authority",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<Void> deleteInventoryItem(
            @Parameter(description = "Unique ID of the inventory item to delete", required = true)
            @PathVariable UUID id) { // Changed UUID to UUID
        inventoryService.deleteInventoryItem(id);
        return ResponseEntity.noContent().build();
    }

    // --- Product Management ---
    @PostMapping("/products")
//    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    @Operation(
            summary = "Create a new product",
            description = "Adds a new product definition to the system. This is master data, not an inventory record.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Product created successfully", content = @Content(schema = @Schema(implementation = ProductDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or missing required fields", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_CREATE_PRODUCT' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Product with given SKU or Barcode already exists", content = @Content)
            }
    )
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductDTO createDTO) {
        ProductDTO newProduct = inventoryService.createProduct(createDTO);
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }

    @GetMapping("/products")
//    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(
            summary = "Get all products",
            description = "Retrieves a list of all products, optionally including inactive ones.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Products retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductDTO.class)))),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_VIEW_PRODUCT' authority", content = @Content)
            }
    )
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            @Parameter(description = "Set to true to include inactive products. Defaults to false.")
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<ProductDTO> products = inventoryService.getAllProducts(includeInactive);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{id}")
//    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(
            summary = "Get product by ID",
            description = "Retrieves a product definition by its unique ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product retrieved successfully", content = @Content(schema = @Schema(implementation = ProductDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_VIEW_PRODUCT' authority", content = @Content)
            }
    )
    public ResponseEntity<ProductDTO> getProductById(@PathVariable UUID id) {
        ProductDTO product = inventoryService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/products/{id}")
//    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    @Operation(
            summary = "Update an existing product",
            description = "Updates the details of an existing product definition.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product updated successfully", content = @Content(schema = @Schema(implementation = ProductDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or missing required fields", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_UPDATE_PRODUCT' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Product SKU or Barcode already in use", content = @Content)
            }
    )
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable UUID id, @Valid @RequestBody UpdateProductDTO updateDTO) {
        ProductDTO updatedProduct = inventoryService.updateProduct(id, updateDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/products/{id}")
//    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    @Operation(
            summary = "Logically delete a product",
            description = "Marks a product as inactive. It does not physically remove it from the database.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Product logically deleted successfully", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_DELETE_PRODUCT' authority", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        inventoryService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // --- Category Management ---
    @PostMapping("/categories")
//    @PreAuthorize("hasAuthority('CATEGORY_WRITE')")
    @Operation(
            summary = "Create a new product category",
            description = "Adds a new category definition.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Category created successfully", content = @Content(schema = @Schema(implementation = CategoryDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_CREATE_CATEGORY' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Category with this name already exists", content = @Content)
            }
    )
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CreateCategoryDTO createDTO) {
        CategoryDTO newCategory = inventoryService.createCategory(createDTO);
        return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
    }

    @GetMapping("/categories")
//    @PreAuthorize("hasAuthority('CATEGORY_READ')")
    @Operation(
            summary = "Get all product categories",
            description = "Retrieves a list of all defined product categories.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryDTO.class)))),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_VIEW_CATEGORY' authority", content = @Content)
            }
    )
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = inventoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/{id}")
//    @PreAuthorize("hasAuthority('CATEGORY_READ')")
    @Operation(
            summary = "Get category by ID",
            description = "Retrieves a category by its unique ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category retrieved successfully", content = @Content(schema = @Schema(implementation = CategoryDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_VIEW_CATEGORY' authority", content = @Content)
            }
    )
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable UUID id) {
        CategoryDTO category = inventoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @PutMapping("/categories/{id}")
//    @PreAuthorize("hasAuthority('CATEGORY_WRITE')")
    @Operation(
            summary = "Update an existing category",
            description = "Updates the details of an existing category.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category updated successfully", content = @Content(schema = @Schema(implementation = CategoryDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_UPDATE_CATEGORY' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Category name already in use", content = @Content)
            }
    )
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable UUID id, @Valid @RequestBody UpdateCategoryDTO updateDTO) {
        CategoryDTO updatedCategory = inventoryService.updateCategory(id, updateDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/categories/{id}")
//    @PreAuthorize("hasAuthority('CATEGORY_WRITE')")
    @Operation(
            summary = "Delete a category",
            description = "Deletes a category by its unique ID. Fails if products are associated.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Category deleted successfully", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_DELETE_CATEGORY' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Category has associated products", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        inventoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // --- Supplier Management ---
    @PostMapping("/suppliers")
//    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    @Operation(
            summary = "Create a new supplier",
            description = "Adds a new supplier to the system.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Supplier created successfully", content = @Content(schema = @Schema(implementation = SupplierDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_CREATE_SUPPLIER' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Supplier with this name already exists", content = @Content)
            }
    )
    public ResponseEntity<SupplierDTO> createSupplier(@Valid @RequestBody CreateSupplierDTO createDTO) {
        SupplierDTO newSupplier = inventoryService.createSupplier(createDTO);
        return new ResponseEntity<>(newSupplier, HttpStatus.CREATED);
    }

    @GetMapping("/suppliers")
//    @PreAuthorize("hasAuthority('SUPPLIER_READ')")
    @Operation(
            summary = "Get all suppliers",
            description = "Retrieves a list of all registered suppliers.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Suppliers retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SupplierDTO.class)))),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_VIEW_SUPPLIER' authority", content = @Content)
            }
    )
    public ResponseEntity<List<SupplierDTO>> getAllSuppliers() {
        List<SupplierDTO> suppliers = inventoryService.getAllSuppliers();
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/suppliers/{id}")
//    @PreAuthorize("hasAuthority('SUPPLIER_READ')")
    @Operation(
            summary = "Get supplier by ID",
            description = "Retrieves a supplier by its unique ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Supplier retrieved successfully", content = @Content(schema = @Schema(implementation = SupplierDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Supplier not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_VIEW_SUPPLIER' authority", content = @Content)
            }
    )
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable UUID id) {
        SupplierDTO supplier = inventoryService.getSupplierById(id);
        return ResponseEntity.ok(supplier);
    }

    @PutMapping("/suppliers/{id}")
//    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    @Operation(
            summary = "Update an existing supplier",
            description = "Updates the details of an existing supplier.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Supplier updated successfully", content = @Content(schema = @Schema(implementation = SupplierDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Supplier not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_UPDATE_SUPPLIER' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Supplier name already in use", content = @Content)
            }
    )
    public ResponseEntity<SupplierDTO> updateSupplier(@PathVariable UUID id, @Valid @RequestBody UpdateSupplierDTO updateDTO) {
        SupplierDTO updatedSupplier = inventoryService.updateSupplier(id, updateDTO);
        return ResponseEntity.ok(updatedSupplier);
    }

    @DeleteMapping("/suppliers/{id}")
//    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    @Operation(
            summary = "Delete a supplier",
            description = "Deletes a supplier by its unique ID. Fails if purchase orders are associated.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Supplier deleted successfully", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Supplier not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_DELETE_SUPPLIER' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Supplier has associated purchase orders", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteSupplier(@PathVariable UUID id) {
        inventoryService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    // --- Location Management ---
    @PostMapping("/locations")
//    @PreAuthorize("hasAuthority('LOCATION_WRITE')")
    @Operation(
            summary = "Create a new inventory location",
            description = "Adds a new physical or logical location for inventory.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Location created successfully", content = @Content(schema = @Schema(implementation = LocationDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_CREATE_LOCATION' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Location with this name already exists", content = @Content)
            }
    )
    public ResponseEntity<LocationDTO> createLocation(@Valid @RequestBody CreateLocationDTO createDTO) {
        LocationDTO newLocation = inventoryService.createLocation(createDTO);
        return new ResponseEntity<>(newLocation, HttpStatus.CREATED);
    }

    @GetMapping("/locations")
//    @PreAuthorize("hasAuthority('LOCATION_READ')")
    @Operation(
            summary = "Get all inventory locations",
            description = "Retrieves a list of all defined inventory locations.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Locations retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = LocationDTO.class)))),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_VIEW_LOCATION' authority", content = @Content)
            }
    )
    public ResponseEntity<List<LocationDTO>> getAllLocations() {
        List<LocationDTO> locations = inventoryService.getAllLocations();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/locations/{id}")
//    @PreAuthorize("hasAuthority('LOCATION_READ')")
    @Operation(
            summary = "Get location by ID",
            description = "Retrieves a location by its unique ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Location retrieved successfully", content = @Content(schema = @Schema(implementation = LocationDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Location not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_VIEW_LOCATION' authority", content = @Content)
            }
    )
    public ResponseEntity<LocationDTO> getLocationById(@PathVariable UUID id) {
        LocationDTO location = inventoryService.getLocationById(id);
        return ResponseEntity.ok(location);
    }

    @PutMapping("/locations/{id}")
//    @PreAuthorize("hasAuthority('LOCATION_WRITE')")
    @Operation(
            summary = "Update an existing location",
            description = "Updates the details of an existing location.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Location updated successfully", content = @Content(schema = @Schema(implementation = LocationDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Location not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_UPDATE_LOCATION' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Location name already in use", content = @Content)
            }
    )
    public ResponseEntity<LocationDTO> updateLocation(@PathVariable UUID id, @Valid @RequestBody UpdateLocationDTO updateDTO) {
        LocationDTO updatedLocation = inventoryService.updateLocation(id, updateDTO);
        return ResponseEntity.ok(updatedLocation);
    }

    @DeleteMapping("/locations/{id}")
//    @PreAuthorize("hasAuthority('LOCATION_WRITE')")
    @Operation(
            summary = "Delete a location",
            description = "Deletes a location by its unique ID. Fails if inventory items or damage records are associated.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Location deleted successfully", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Location not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_DELETE_LOCATION' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Location has associated inventory items or damage records", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteLocation(@PathVariable UUID id) {
        inventoryService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

    // --- Unit of Measure Management ---
    @PostMapping("/units-of-measure")
//    @PreAuthorize("hasAuthority('UOM_WRITE')")
    @Operation(
            summary = "Create a new Unit of Measure",
            description = "Adds a new unit of measure (e.g., 'kilogram', 'piece').",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Unit of Measure created successfully", content = @Content(schema = @Schema(implementation = UnitOfMeasureDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'UOM_WRITE' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Unit of Measure with this name or abbreviation already exists", content = @Content)
            }
    )
    public ResponseEntity<UnitOfMeasureDTO> createUnitOfMeasure(@Valid @RequestBody CreateUnitOfMeasureDTO createDTO) {
        UnitOfMeasureDTO newUoM = inventoryService.createUnitOfMeasure(createDTO);
        return new ResponseEntity<>(newUoM, HttpStatus.CREATED);
    }

    @GetMapping("/units-of-measure")
//    @PreAuthorize("hasAuthority('UOM_READ')")
    @Operation(
            summary = "Get all Units of Measure",
            description = "Retrieves a list of all defined units of measure.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Units of Measure retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UnitOfMeasureDTO.class)))),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'UOM_READ' authority", content = @Content)
            }
    )
    public ResponseEntity<List<UnitOfMeasureDTO>> getAllUnitOfMeasures() {
        List<UnitOfMeasureDTO> uoms = inventoryService.getAllUnitOfMeasures();
        return ResponseEntity.ok(uoms);
    }

    @GetMapping("/units-of-measure/{id}")
//    @PreAuthorize("hasAuthority('UOM_READ')")
    @Operation(
            summary = "Get Unit of Measure by ID",
            description = "Retrieves a Unit of Measure by its unique ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Unit of Measure retrieved successfully", content = @Content(schema = @Schema(implementation = UnitOfMeasureDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Unit of Measure not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'UOM_READ' authority", content = @Content)
            }
    )
    public ResponseEntity<UnitOfMeasureDTO> getUnitOfMeasureById(@PathVariable UUID id) {
        UnitOfMeasureDTO uom = inventoryService.getUnitOfMeasureById(id);
        return ResponseEntity.ok(uom);
    }

    @PutMapping("/units-of-measure/{id}")
//    @PreAuthorize("hasAuthority('UOM_WRITE')")
    @Operation(
            summary = "Update an existing Unit of Measure",
            description = "Updates the details of an existing Unit of Measure.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Unit of Measure updated successfully", content = @Content(schema = @Schema(implementation = UnitOfMeasureDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Unit of Measure not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'UOM_WRITE' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Unit of Measure name or abbreviation already in use", content = @Content)
            }
    )
    public ResponseEntity<UnitOfMeasureDTO> updateUnitOfMeasure(@PathVariable UUID id, @Valid @RequestBody UpdateUnitOfMeasureDTO updateDTO) {
        UnitOfMeasureDTO updatedUoM = inventoryService.updateUnitOfMeasure(id, updateDTO);
        return ResponseEntity.ok(updatedUoM);
    }

    @DeleteMapping("/units-of-measure/{id}")
//    @PreAuthorize("hasAuthority('UOM_WRITE')")
    @Operation(
            summary = "Delete a Unit of Measure",
            description = "Deletes a Unit of Measure by its unique ID. Fails if products are associated.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Unit of Measure deleted successfully", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Unit of Measure not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_DELETE_UOM' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Unit of Measure has associated products", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteUnitOfMeasure(@PathVariable UUID id) {
        inventoryService.deleteUnitOfMeasure(id);
        return ResponseEntity.noContent().build();
    }

    // --- Purchase Order Management ---
    @PostMapping("/purchase-orders")
//    @PreAuthorize("hasAuthority('PO_WRITE')")
    @Operation(
            summary = "Create a new purchase order",
            description = "Submits a new purchase order to a supplier for specific products.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Purchase Order created successfully", content = @Content(schema = @Schema(implementation = PurchaseOrderDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or missing required fields", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Supplier or Product not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_CREATE_PO' authority", content = @Content)
            }
    )
    public ResponseEntity<PurchaseOrderDTO> createPurchaseOrder(@Valid @RequestBody CreatePurchaseOrderDTO createDTO) {
        PurchaseOrderDTO newPO = inventoryService.createPurchaseOrder(createDTO);
        return new ResponseEntity<>(newPO, HttpStatus.CREATED);
    }

    @GetMapping("/purchase-orders")
//    @PreAuthorize("hasAuthority('PO_READ')")
    @Operation(
            summary = "Get all purchase orders",
            description = "Retrieves a list of all purchase orders, with optional status filtering.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Purchase Orders retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PurchaseOrderDTO.class)))),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_VIEW_PO' authority", content = @Content)
            }
    )
    public ResponseEntity<List<PurchaseOrderDTO>> getAllPurchaseOrders(
            @Parameter(description = "Filter purchase orders by their status (e.g., PENDING, RECEIVED_PARTIAL, RECEIVED_COMPLETE, CANCELLED).")
            @RequestParam(required = false) PurchaseOrder.PurchaseOrderStatus statusFilter) {
        List<PurchaseOrderDTO> purchaseOrders = inventoryService.getAllPurchaseOrders(statusFilter);
        return ResponseEntity.ok(purchaseOrders);
    }

    @GetMapping("/purchase-orders/{id}")
//    @PreAuthorize("hasAuthority('PO_READ')")
    @Operation(
            summary = "Get purchase order by ID",
            description = "Retrieves a purchase order by its unique ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Purchase Order retrieved successfully", content = @Content(schema = @Schema(implementation = PurchaseOrderDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Purchase Order not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_VIEW_PO' authority", content = @Content)
            }
    )
    public ResponseEntity<PurchaseOrderDTO> getPurchaseOrderById(@PathVariable UUID id) {
        PurchaseOrderDTO purchaseOrder = inventoryService.getPurchaseOrderById(id);
        return ResponseEntity.ok(purchaseOrder);
    }

    @PutMapping("/purchase-orders/{id}")
//    @PreAuthorize("hasAuthority('PO_WRITE')")
    @Operation(
            summary = "Update an existing purchase order",
            description = "Updates the details of an existing purchase order. Cannot update completed or cancelled orders.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Purchase Order updated successfully", content = @Content(schema = @Schema(implementation = PurchaseOrderDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Purchase Order not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_UPDATE_PO' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Cannot update completed/cancelled order", content = @Content)
            }
    )
    public ResponseEntity<PurchaseOrderDTO> updatePurchaseOrder(@PathVariable UUID id, @Valid @RequestBody UpdatePurchaseOrderDTO updateDTO) {
        PurchaseOrderDTO updatedPO = inventoryService.updatePurchaseOrder(id, updateDTO);
        return ResponseEntity.ok(updatedPO);
    }

    @PostMapping("/purchase-orders/{id}/receipt")
//    @PreAuthorize("hasAuthority('PO_WRITE')")
    @Operation(
            summary = "Process receipt of items for a purchase order",
            description = "Records the reception of items for a specific purchase order, updating inventory stock.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Items received and inventory updated successfully", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Invalid input (e.g., negative quantity, exceeding ordered quantity)", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Purchase Order, Product, or Location not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_RECEIVE_PO' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Cannot receive items for cancelled/completed PO", content = @Content)
            }
    )
    public ResponseEntity<Void> processPurchaseOrderReceipt(
            @Parameter(description = "ID of the Purchase Order to receive items for", required = true)
            @PathVariable UUID id,
            @Parameter(description = "List of items received with their quantities, batch, and expiration", required = true)
            @Valid @RequestBody List<PurchaseOrderItemDTO> receivedItems) {
        inventoryService.processPurchaseOrderReceipt(id, receivedItems);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/purchase-orders/{id}/cancel")
//    @PreAuthorize("hasAuthority('PO_WRITE')")
    @Operation(
            summary = "Cancel a purchase order",
            description = "Changes the status of a purchase order to 'CANCELLED'. Fails if already completed.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Purchase Order cancelled successfully", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Purchase Order not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_CANCEL_PO' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Cannot cancel completed PO", content = @Content)
            }
    )
    public ResponseEntity<Void> cancelPurchaseOrder(@PathVariable UUID id) {
        inventoryService.cancelPurchaseOrder(id);
        return ResponseEntity.ok().build();
    }

    // --- Sales Management ---
    @PostMapping("/sales")
//    @PreAuthorize("hasAuthority('SALE_WRITE')")
    @Operation(
            summary = "Process a new sale transaction",
            description = "Records a sale, decrements inventory stock, and calculates total amount. This is a transactional operation.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sale processed successfully", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Invalid input or insufficient stock", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Product or User not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_PROCESS_SALE' authority", content = @Content)
            }
    )
    public ResponseEntity<Void> processSale(@Valid @RequestBody CreateSaleDTO createDTO) {
        inventoryService.processSale(createDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sales/{id}")
//    @PreAuthorize("hasAuthority('SALE_READ')")
    @Operation(
            summary = "Get sale by ID",
            description = "Retrieves details of a specific sale transaction.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sale retrieved successfully", content = @Content(schema = @Schema(implementation = SaleDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Sale not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_VIEW_SALE' authority", content = @Content)
            }
    )
    public ResponseEntity<SaleDTO> getSaleById(@PathVariable UUID id) {
        SaleDTO sale = inventoryService.getSaleById(id);
        return ResponseEntity.ok(sale);
    }

    @GetMapping("/sales/by-date-range")
//    @PreAuthorize("hasAuthority('SALE_READ')")
    @Operation(
            summary = "Get sales within a date range",
            description = "Retrieves a list of sales that occurred between the specified start and end dates.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sales retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SaleDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "Invalid date format", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'SALE_READ' authority", content = @Content)
            }
    )
    public ResponseEntity<List<SaleDTO>> getSalesByDateRange(
            @Parameter(description = "Start date (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @Parameter(description = "End date (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate) {
        List<SaleDTO> sales = inventoryService.getSalesByDateRange(startDate, endDate);
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/sales/by-product/{productTemplateId}")
//    @PreAuthorize("hasAuthority('SALE_READ')")
    @Operation(
            summary = "Get sales for a specific product",
            description = "Retrieves all sales that included a particular product.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sales retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SaleDTO.class)))),
                    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'SALE_READ' authority", content = @Content)
            }
    )
    public ResponseEntity<List<SaleDTO>> getSalesForProduct(@PathVariable UUID productTemplateId) {
        List<SaleDTO> sales = inventoryService.getSalesForProduct(productTemplateId);
        return ResponseEntity.ok(sales);
    }

    // --- Discount Management ---
    @PostMapping("/discounts")
//    @PreAuthorize("hasAuthority('DISCOUNT_WRITE')")
    @Operation(
            summary = "Create a new discount",
            description = "Adds a new discount for a product or category.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Discount created successfully", content = @Content(schema = @Schema(implementation = DiscountDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Product or Category not found (if specified)", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_CREATE_DISCOUNT' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Discount with this name already exists", content = @Content)
            }
    )
    public ResponseEntity<DiscountDTO> createDiscount(@Valid @RequestBody CreateDiscountDTO createDTO) {
        DiscountDTO newDiscount = inventoryService.createDiscount(createDTO);
        return new ResponseEntity<>(newDiscount, HttpStatus.CREATED);
    }

    @GetMapping("/discounts")
//    @PreAuthorize("hasAuthority('DISCOUNT_READ')")
    @Operation(
            summary = "Get all discounts",
            description = "Retrieves a list of all discounts, with an option to include inactive ones.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Discounts retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DiscountDTO.class)))),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'DISCOUNT_READ' authority", content = @Content)
            }
    )
    public ResponseEntity<List<DiscountDTO>> getAllDiscounts(
            @Parameter(description = "Set to true to include inactive discounts. Defaults to false.")
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<DiscountDTO> discounts = inventoryService.getAllDiscounts(includeInactive);
        return ResponseEntity.ok(discounts);
    }

    @GetMapping("/discounts/{id}")
//    @PreAuthorize("hasAuthority('DISCOUNT_READ')")
    @Operation(
            summary = "Get discount by ID",
            description = "Retrieves a discount by its unique ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Discount retrieved successfully", content = @Content(schema = @Schema(implementation = DiscountDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Discount not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'DISCOUNT_READ' authority", content = @Content)
            }
    )
    public ResponseEntity<DiscountDTO> getDiscountById(@PathVariable UUID id) {
        DiscountDTO discount = inventoryService.getDiscountById(id);
        return ResponseEntity.ok(discount);
    }

    @PutMapping("/discounts/{id}")
//    @PreAuthorize("hasAuthority('DISCOUNT_WRITE')")
    @Operation(
            summary = "Update an existing discount",
            description = "Updates the details of an existing discount.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Discount updated successfully", content = @Content(schema = @Schema(implementation = DiscountDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Discount not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_UPDATE_DISCOUNT' authority", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Conflict: Discount name already in use", content = @Content)
            }
    )
    public ResponseEntity<DiscountDTO> updateDiscount(@PathVariable UUID id, @Valid @RequestBody UpdateDiscountDTO updateDTO) {
        DiscountDTO updatedDiscount = inventoryService.updateDiscount(id, updateDTO);
        return ResponseEntity.ok(updatedDiscount);
    }

    @PatchMapping("/discounts/{id}/deactivate")
//    @PreAuthorize("hasAuthority('DISCOUNT_WRITE')")
    @Operation(
            summary = "Deactivate a discount",
            description = "Sets a discount's active status to false.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Discount deactivated successfully", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Discount not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_DEACTIVATE_DISCOUNT' authority", content = @Content)
            }
    )
    public ResponseEntity<Void> deactivateDiscount(@PathVariable UUID id) {
        inventoryService.deactivateDiscount(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/discounts/active/by-product/{productId}")
//    @PreAuthorize("hasAuthority('DISCOUNT_READ')")
    @Operation(
            summary = "Get active discounts for a product",
            description = "Retrieves active discounts currently applicable to a specific product.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Discounts retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DiscountDTO.class)))),
                    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'DISCOUNT_READ' authority", content = @Content)
            }
    )
    public ResponseEntity<List<DiscountDTO>> getActiveDiscountsForProduct(@PathVariable UUID productId) {
        List<DiscountDTO> discounts = inventoryService.getActiveDiscountsForProduct(productId);
        return ResponseEntity.ok(discounts);
    }

    @GetMapping("/discounts/active/by-category/{categoryId}")
//    @PreAuthorize("hasAuthority('DISCOUNT_READ')")
    @Operation(
            summary = "Get active discounts for a category",
            description = "Retrieves active discounts currently applicable to a specific product category.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Discounts retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DiscountDTO.class)))),
                    @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'DISCOUNT_READ' authority", content = @Content)
            }
    )
    public ResponseEntity<List<DiscountDTO>> getActiveDiscountsForCategory(@PathVariable UUID categoryId) {
        List<DiscountDTO> discounts = inventoryService.getActiveDiscountsForCategory(categoryId);
        return ResponseEntity.ok(discounts);
    }

    // --- Damage/Loss Management ---
    @PostMapping("/damage-losses")
//    @PreAuthorize("hasAuthority('DAMAGE_LOSS_WRITE')")
    @Operation(
            summary = "Record a damage or loss event",
            description = "Records a quantity of a product as damaged or lost, decrementing inventory stock.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Damage/Loss recorded successfully", content = @Content(schema = @Schema(implementation = DamageLossDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or insufficient stock at location", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Product, Location, or User not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_RECORD_DAMAGE_LOSS' authority", content = @Content)
            }
    )
    public ResponseEntity<DamageLossDTO> recordDamageLoss(@Valid @RequestBody CreateDamageLossDTO createDTO) {
        DamageLossDTO newRecord = inventoryService.recordDamageLoss(createDTO);
        return new ResponseEntity<>(newRecord, HttpStatus.CREATED);
    }

    @GetMapping("/damage-losses")
//    @PreAuthorize("hasAuthority('DAMAGE_LOSS_READ')")
    @Operation(
            summary = "Get all damage/loss records",
            description = "Retrieves a list of all recorded damage and loss events within a date range.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Damage/Loss records retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DamageLossDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "Invalid date format", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'DAMAGE_LOSS_READ' authority", content = @Content)
            }
    )
    public ResponseEntity<List<DamageLossDTO>> getAllDamageLossRecords(
            @Parameter(description = "Start date (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @Parameter(description = "End date (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate) {
        List<DamageLossDTO> records = inventoryService.getAllDamageLossRecords(startDate, endDate);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/damage-losses/{id}")
//    @PreAuthorize("hasAuthority('DAMAGE_LOSS_READ')")
    @Operation(
            summary = "Get damage/loss record by ID",
            description = "Retrieves a specific damage or loss record by its unique ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Damage/Loss record retrieved successfully", content = @Content(schema = @Schema(implementation = DamageLossDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Damage/Loss record not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'DAMAGE_LOSS_READ' authority", content = @Content)
            }
    )
    public ResponseEntity<DamageLossDTO> getDamageLossRecordById(@PathVariable UUID id) {
        DamageLossDTO record = inventoryService.getDamageLossRecordById(id);
        return ResponseEntity.ok(record);
    }

    // --- Stock Information & Checks ---
    @GetMapping("/stock/total/{productId}")
//    @PreAuthorize("hasAuthority('STOCK_CHECK_READ')")
    @Operation(
//            summary = "Get total stock quantity for a product",
            description = "Retrieves the aggregated quantity of a specific product across all inventory locations.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Total stock quantity retrieved successfully", content = @Content(schema = @Schema(implementation = Integer.class))),
                    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_VIEW_STOCK_LEVELS' authority", content = @Content)
            }
    )
    public ResponseEntity<Integer> getTotalStockQuantity(@PathVariable UUID productId) {
        int totalQuantity = inventoryService.getTotalStockQuantity(productId);
        return ResponseEntity.ok(totalQuantity);
    }

    @GetMapping("/stock/at-location/{productId}/{locationId}")
//    @PreAuthorize("hasAuthority('STOCK_CHECK_READ')")
    @Operation(
            summary = "Get stock quantity for a product at a specific location",
            description = "Retrieves the quantity of a specific product available at a particular inventory location.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Stock quantity at location retrieved successfully", content = @Content(schema = @Schema(implementation = Integer.class))),
                    @ApiResponse(responseCode = "404", description = "Product or Location not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_VIEW_STOCK_LEVELS' authority", content = @Content)
            }
    )
    public ResponseEntity<Integer> getStockQuantityAtLocation(
            @PathVariable UUID productId,
            @PathVariable UUID locationId) {
        int quantity = inventoryService.getStockQuantityAtLocation(productId, locationId);
        return ResponseEntity.ok(quantity);
    }

    @GetMapping("/stock/check-availability")
//    @PreAuthorize("hasAuthority('STOCK_CHECK_READ')")
    @Operation(
            summary = "Check overall stock availability for a product",
            description = "Checks if a requested quantity of a product is available across all locations.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Stock availability check successful", content = @Content(schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid quantity needed (must be positive)", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_CHECK_STOCK_AVAILABILITY' authority", content = @Content)
            }
    )
    public ResponseEntity<Boolean> checkStockAvailability(
            @Parameter(description = "ID of the product to check stock for", required = true)
            @RequestParam UUID productId,
            @Parameter(description = "Quantity of the product needed", required = true)
            @RequestParam int quantityNeeded) {
        boolean available = inventoryService.checkStockAvailability(productId, quantityNeeded);
        return ResponseEntity.ok(available);
    }

    @GetMapping("/stock/check-availability-at-location")
//    @PreAuthorize("hasAuthority('STOCK_CHECK_READ')")
    @Operation(
            summary = "Check stock availability for a product at a specific location",
            description = "Checks if a requested quantity of a product is available at a particular location.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Stock availability check successful", content = @Content(schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid quantity needed (must be positive)", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Product or Location not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_CHECK_STOCK_AVAILABILITY' authority", content = @Content)
            }
    )
    public ResponseEntity<Boolean> checkStockAvailabilityAtLocation(
            @Parameter(description = "ID of the product to check stock for", required = true)
            @RequestParam UUID productId,
            @Parameter(description = "ID of the location to check stock at", required = true)
            @RequestParam UUID locationId,
            @Parameter(description = "Quantity of the product needed", required = true)
            @RequestParam int quantityNeeded) {
        boolean available = inventoryService.checkStockAvailabilityAtLocation(productId, locationId, quantityNeeded);
        return ResponseEntity.ok(available);
    }

    @GetMapping("/stock/product-retail-price/{productId}")
//    @PreAuthorize("hasAuthority('STOCK_CHECK_READ')")
    @Operation(
            summary = "Get retail price of a product",
            description = "Retrieves the retail price of a product.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Retail price retrieved successfully", content = @Content(schema = @Schema(type = "number", format = "double"))),
                    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden: User does not have 'PERM_VIEW_PRODUCT_PRICES' authority", content = @Content)
            }
    )
    public ResponseEntity<java.math.BigDecimal> getInventoryItemRetailPrice(@PathVariable UUID productId) {
        java.math.BigDecimal retailPrice = inventoryService.getInventoryItemRetailPrice(productId);
        return ResponseEntity.ok(retailPrice);
    }
}