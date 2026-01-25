package com.store.mgmt.modules.products.application;

import com.store.mgmt.modules.products.application.dto.ProductVariantDTO;
import com.store.mgmt.modules.products.application.query.GetProductVariantsHandler;
import com.store.mgmt.modules.products.application.query.GetProductVariantsQuery;
import com.store.mgmt.modules.products.domain.model.*;
import com.store.mgmt.modules.products.domain.repository.ProductTemplateRepository;
import com.store.mgmt.modules.products.domain.repository.ProductVariantRepository;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetProductVariantsHandler")
class GetProductVariantsHandlerTest {

    @Mock
    private ProductVariantRepository variantRepository;

    @Mock
    private ProductTemplateRepository templateRepository;

    @Mock
    private TenantContext tenantContext;

    private MockedStatic<TenantContext> tenantContextMock;

    private GetProductVariantsHandler handler;

    private UUID organizationId;
    private UUID templateId;
    private UUID variantId;

    @BeforeEach
    void setUp() {
        handler = new GetProductVariantsHandler(variantRepository, templateRepository);

        organizationId = UUID.randomUUID();
        templateId = UUID.randomUUID();
        variantId = UUID.randomUUID();

        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::current).thenReturn(tenantContext);
        when(tenantContext.organizationId()).thenReturn(organizationId);
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("Should return variants with template name populated")
        void returnsVariantsWithTemplateName() {
            // Given
            String expectedTemplateName = "Test Product Template";

            ProductVariant variant = createTestVariant(variantId, templateId);
            ProductTemplate template = createTestTemplate(templateId, expectedTemplateName);

            when(variantRepository.findByOrganizationId(any(OrganizationId.class)))
                    .thenReturn(List.of(variant));
            when(templateRepository.findByOrganizationId(any(OrganizationId.class)))
                    .thenReturn(List.of(template));

            GetProductVariantsQuery query = new GetProductVariantsQuery(null, false, 0, 10);

            // When
            List<ProductVariantDTO> results = handler.handle(query);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getTemplateName()).isEqualTo(expectedTemplateName);
            assertThat(results.get(0).getSku()).isEqualTo("TEST-SKU-001");
        }

        @Test
        @DisplayName("Should handle multiple variants with different templates")
        void handlesMultipleVariantsWithDifferentTemplates() {
            // Given
            UUID templateId1 = UUID.randomUUID();
            UUID templateId2 = UUID.randomUUID();
            UUID variantId1 = UUID.randomUUID();
            UUID variantId2 = UUID.randomUUID();

            ProductVariant variant1 = createTestVariant(variantId1, templateId1);
            ProductVariant variant2 = createTestVariantWithSku(variantId2, templateId2, "TEST-SKU-002");
            ProductTemplate template1 = createTestTemplate(templateId1, "Product A");
            ProductTemplate template2 = createTestTemplate(templateId2, "Product B");

            when(variantRepository.findByOrganizationId(any(OrganizationId.class)))
                    .thenReturn(List.of(variant1, variant2));
            when(templateRepository.findByOrganizationId(any(OrganizationId.class)))
                    .thenReturn(List.of(template1, template2));

            GetProductVariantsQuery query = new GetProductVariantsQuery(null, false, 0, 10);

            // When
            List<ProductVariantDTO> results = handler.handle(query);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getTemplateName()).isEqualTo("Product A");
            assertThat(results.get(1).getTemplateName()).isEqualTo("Product B");
        }

        @Test
        @DisplayName("Should return null template name when template not found")
        void returnsNullTemplateNameWhenNotFound() {
            // Given
            ProductVariant variant = createTestVariant(variantId, templateId);

            when(variantRepository.findByOrganizationId(any(OrganizationId.class)))
                    .thenReturn(List.of(variant));
            when(templateRepository.findByOrganizationId(any(OrganizationId.class)))
                    .thenReturn(List.of()); // No templates found

            GetProductVariantsQuery query = new GetProductVariantsQuery(null, false, 0, 10);

            // When
            List<ProductVariantDTO> results = handler.handle(query);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getTemplateName()).isNull();
        }

        @Test
        @DisplayName("Should return empty list when no variants exist")
        void returnsEmptyListWhenNoVariants() {
            // Given
            when(variantRepository.findByOrganizationId(any(OrganizationId.class)))
                    .thenReturn(List.of());

            GetProductVariantsQuery query = new GetProductVariantsQuery(null, false, 0, 10);

            // When
            List<ProductVariantDTO> results = handler.handle(query);

            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should respect pagination")
        void respectsPagination() {
            // Given
            UUID variantId1 = UUID.randomUUID();
            UUID variantId2 = UUID.randomUUID();
            UUID variantId3 = UUID.randomUUID();

            ProductVariant variant1 = createTestVariantWithSku(variantId1, templateId, "SKU-001");
            ProductVariant variant2 = createTestVariantWithSku(variantId2, templateId, "SKU-002");
            ProductVariant variant3 = createTestVariantWithSku(variantId3, templateId, "SKU-003");
            ProductTemplate template = createTestTemplate(templateId, "Test Template");

            when(variantRepository.findByOrganizationId(any(OrganizationId.class)))
                    .thenReturn(List.of(variant1, variant2, variant3));
            when(templateRepository.findByOrganizationId(any(OrganizationId.class)))
                    .thenReturn(List.of(template));

            // Request page 1 with size 2
            GetProductVariantsQuery query = new GetProductVariantsQuery(null, false, 1, 2);

            // When
            List<ProductVariantDTO> results = handler.handle(query);

            // Then
            assertThat(results).hasSize(1); // Only 1 item on page 1 (0-indexed: items 2-3, but only 3 total)
            assertThat(results.get(0).getSku()).isEqualTo("SKU-003");
        }
    }

    private ProductVariant createTestVariant(UUID id, UUID templateId) {
        return createTestVariantWithSku(id, templateId, "TEST-SKU-001");
    }

    private ProductVariant createTestVariantWithSku(UUID id, UUID templateId, String sku) {
        return ProductVariant.reconstitute(
                ProductVariantId.of(id),
                ProductTemplateId.of(templateId),
                Sku.of(sku),
                null, // barcode
                Money.of(BigDecimal.valueOf(10.00)),
                Money.of(BigDecimal.valueOf(15.00)),
                ProductAttributes.empty(),
                true,
                OrganizationId.of(organizationId),
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
    }

    private ProductTemplate createTestTemplate(UUID id, String name) {
        return ProductTemplate.reconstitute(
                ProductTemplateId.of(id),
                name,
                "Test description",
                CategoryId.of(UUID.randomUUID()),
                null, // brandId
                UnitOfMeasureId.of(UUID.randomUUID()),
                null, // imageUrl
                10, // reorderPoint
                false, // requiresExpiry
                true, // active
                ProductAttributes.empty(),
                OrganizationId.of(organizationId),
                List.of(), // variantIds
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
    }
}
