package com.store.mgmt.modules.inventory.application;

import com.store.mgmt.modules.inventory.application.dto.InventoryItemDTO;
import com.store.mgmt.modules.inventory.application.query.GetInventoryItemsHandler;
import com.store.mgmt.modules.inventory.application.query.GetInventoryItemsQuery;
import com.store.mgmt.modules.inventory.domain.model.*;
import com.store.mgmt.modules.inventory.domain.repository.InventoryItemRepository;
import com.store.mgmt.modules.inventory.domain.repository.StockLevelRepository;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetInventoryItemsHandler")
class GetInventoryItemsHandlerTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private StockLevelRepository stockLevelRepository;

    @Mock
    private TenantContext tenantContext;

    private MockedStatic<TenantContext> tenantContextMock;

    private GetInventoryItemsHandler handler;

    private UUID storeId;
    private UUID organizationId;

    @BeforeEach
    void setUp() {
        handler = new GetInventoryItemsHandler(inventoryItemRepository, stockLevelRepository);

        storeId = UUID.randomUUID();
        organizationId = UUID.randomUUID();

        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::current).thenReturn(tenantContext);
        // Mock requireStore to do nothing (it's a void method for validation)
        doNothing().when(tenantContext).requireStore(any(UUID.class));
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("Should return inventory items with variant SKU and name populated")
        void returnsItemsWithVariantInfo() {
            // Given
            String expectedSku = "TEST-SKU-001";
            String expectedProductName = "Test Product";
            String expectedLocationName = "Warehouse A";

            InventoryItem item = createTestInventoryItem(expectedSku, expectedProductName, expectedLocationName);

            when(inventoryItemRepository.findByStoreId(storeId)).thenReturn(List.of(item));

            GetInventoryItemsQuery query = new GetInventoryItemsQuery(storeId, false, false, null, 0, 10);

            // When
            List<InventoryItemDTO> results = handler.handle(query);

            // Then
            assertThat(results).hasSize(1);
            InventoryItemDTO dto = results.get(0);
            assertThat(dto.variantSku()).isEqualTo(expectedSku);
            assertThat(dto.variantName()).isEqualTo(expectedProductName);
            assertThat(dto.locationName()).isEqualTo(expectedLocationName);
        }

        @Test
        @DisplayName("Should handle null variant gracefully")
        void handlesNullVariant() {
            // Given
            InventoryItem item = createInventoryItemWithoutVariant();

            when(inventoryItemRepository.findByStoreId(storeId)).thenReturn(List.of(item));

            GetInventoryItemsQuery query = new GetInventoryItemsQuery(storeId, false, false, null, 0, 10);

            // When
            List<InventoryItemDTO> results = handler.handle(query);

            // Then
            assertThat(results).hasSize(1);
            InventoryItemDTO dto = results.get(0);
            assertThat(dto.variantSku()).isNull();
            assertThat(dto.variantName()).isNull();
        }

        @Test
        @DisplayName("Should handle null location gracefully")
        void handlesNullLocation() {
            // Given
            InventoryItem item = createInventoryItemWithoutLocation();

            when(inventoryItemRepository.findByStoreId(storeId)).thenReturn(List.of(item));

            GetInventoryItemsQuery query = new GetInventoryItemsQuery(storeId, false, false, null, 0, 10);

            // When
            List<InventoryItemDTO> results = handler.handle(query);

            // Then
            assertThat(results).hasSize(1);
            InventoryItemDTO dto = results.get(0);
            assertThat(dto.locationName()).isNull();
        }

        @Test
        @DisplayName("Should return empty list when no items found")
        void returnsEmptyListWhenNoItems() {
            // Given
            when(inventoryItemRepository.findByStoreId(storeId)).thenReturn(List.of());

            GetInventoryItemsQuery query = new GetInventoryItemsQuery(storeId, false, false, null, 0, 10);

            // When
            List<InventoryItemDTO> results = handler.handle(query);

            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should populate stock level information")
        void populatesStockLevelInfo() {
            // Given
            InventoryItem item = createTestInventoryItemWithStock(100, 10, 90, 20);

            when(inventoryItemRepository.findByStoreId(storeId)).thenReturn(List.of(item));

            GetInventoryItemsQuery query = new GetInventoryItemsQuery(storeId, false, false, null, 0, 10);

            // When
            List<InventoryItemDTO> results = handler.handle(query);

            // Then
            assertThat(results).hasSize(1);
            InventoryItemDTO dto = results.get(0);
            assertThat(dto.onHand()).isEqualTo(100);
            assertThat(dto.reserved()).isEqualTo(10);
            assertThat(dto.available()).isEqualTo(90);
            assertThat(dto.reorderPoint()).isEqualTo(20);
        }

        @Test
        @DisplayName("Should identify low stock items")
        void identifiesLowStockItems() {
            // Given - available (5) <= lowStockThreshold (10)
            InventoryItem item = createTestInventoryItemWithStock(10, 5, 5, 10);

            when(inventoryItemRepository.findByStoreId(storeId)).thenReturn(List.of(item));

            GetInventoryItemsQuery query = new GetInventoryItemsQuery(storeId, false, false, null, 0, 10);

            // When
            List<InventoryItemDTO> results = handler.handle(query);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).isLowStock()).isTrue();
        }

        @Test
        @DisplayName("Should identify expiring soon items")
        void identifiesExpiringSoonItems() {
            // Given - expiry date within 30 days
            InventoryItem item = createTestInventoryItem("SKU-001", "Product", "Location A");
            item.setExpiryDate(LocalDate.now().plusDays(15));

            when(inventoryItemRepository.findByStoreId(storeId)).thenReturn(List.of(item));

            GetInventoryItemsQuery query = new GetInventoryItemsQuery(storeId, false, false, null, 0, 10);

            // When
            List<InventoryItemDTO> results = handler.handle(query);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).isExpiringSoon()).isTrue();
        }
    }

    private InventoryItem createTestInventoryItem(String sku, String productName, String locationName) {
        InventoryItem item = new InventoryItem();
        item.setId(UUID.randomUUID());

        // Create variant with template
        ProductVariant variant = new ProductVariant();
        variant.setId(UUID.randomUUID());
        variant.setSku(sku);
        variant.setOrganizationId(organizationId);

        ProductTemplate template = new ProductTemplate();
        template.setId(UUID.randomUUID());
        template.setName(productName);
        template.setOrganizationId(organizationId);

        variant.setTemplate(template);
        item.setVariant(variant);

        // Create location
        InventoryLocation location = new InventoryLocation();
        location.setId(UUID.randomUUID());
        location.setName(locationName);
        location.setStoreId(storeId);
        item.setLocation(location);

        // Create stock level
        StockLevel stockLevel = new StockLevel();
        stockLevel.setOnHand(0);
        stockLevel.setCommitted(0);
        stockLevel.setAvailable(0);  // Must set explicitly in tests
        stockLevel.setLowStockThreshold(10);
        item.setStockLevel(stockLevel);

        return item;
    }

    private InventoryItem createTestInventoryItemWithStock(int onHand, int committed, int available, int lowStockThreshold) {
        InventoryItem item = createTestInventoryItem("SKU-001", "Product", "Location A");

        StockLevel stockLevel = new StockLevel();
        stockLevel.setOnHand(onHand);
        stockLevel.setCommitted(committed);
        stockLevel.setAvailable(available);  // Must set explicitly in tests (JPA lifecycle not triggered)
        stockLevel.setLowStockThreshold(lowStockThreshold);
        item.setStockLevel(stockLevel);

        return item;
    }

    private InventoryItem createInventoryItemWithoutVariant() {
        InventoryItem item = new InventoryItem();
        item.setId(UUID.randomUUID());
        item.setVariant(null);

        InventoryLocation location = new InventoryLocation();
        location.setId(UUID.randomUUID());
        location.setName("Location");
        location.setStoreId(storeId);
        item.setLocation(location);

        return item;
    }

    private InventoryItem createInventoryItemWithoutLocation() {
        InventoryItem item = new InventoryItem();
        item.setId(UUID.randomUUID());
        item.setLocation(null);

        ProductVariant variant = new ProductVariant();
        variant.setId(UUID.randomUUID());
        variant.setSku("SKU-001");
        item.setVariant(variant);

        return item;
    }
}
