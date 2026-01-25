package com.store.mgmt.modules.products.domain;

import com.store.mgmt.modules.products.domain.model.*;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProductTemplate Domain Model")
class ProductTemplateTest {

    @Mock
    private TenantContext tenantContext;

    private MockedStatic<TenantContext> tenantContextMock;

    private UUID organizationId;
    private CategoryId categoryId;
    private UnitOfMeasureId uomId;
    private BrandId brandId;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        categoryId = CategoryId.of(UUID.randomUUID());
        uomId = UnitOfMeasureId.of(UUID.randomUUID());
        brandId = BrandId.of(UUID.randomUUID());

        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::current).thenReturn(tenantContext);
        when(tenantContext.organizationId()).thenReturn(organizationId);
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Should create product template with all required fields")
        void createsWithRequiredFields() {
            // When
            ProductTemplate template = ProductTemplate.create(
                    "Test Product",
                    "A test product description",
                    categoryId,
                    uomId,
                    null,
                    null,
                    null,
                    false,
                    null
            );

            // Then
            assertThat(template).isNotNull();
            assertThat(template.getId()).isNotNull();
            assertThat(template.getName()).isEqualTo("Test Product");
            assertThat(template.getDescription()).isEqualTo("A test product description");
            assertThat(template.getCategoryId()).isEqualTo(categoryId);
            assertThat(template.getUnitOfMeasureId()).isEqualTo(uomId);
            assertThat(template.isActive()).isTrue();
            assertThat(template.getOrganizationId().getValue()).isEqualTo(organizationId);
        }

        @Test
        @DisplayName("Should create with default reorder point of 10")
        void createsWithDefaultReorderPoint() {
            // When
            ProductTemplate template = ProductTemplate.create(
                    "Test Product",
                    null,
                    categoryId,
                    uomId,
                    null,
                    null,
                    null, // null reorder point
                    false,
                    null
            );

            // Then
            assertThat(template.getReorderPoint()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should create with custom reorder point")
        void createsWithCustomReorderPoint() {
            // When
            ProductTemplate template = ProductTemplate.create(
                    "Test Product",
                    null,
                    categoryId,
                    uomId,
                    null,
                    null,
                    25, // custom reorder point
                    false,
                    null
            );

            // Then
            assertThat(template.getReorderPoint()).isEqualTo(25);
        }

        @Test
        @DisplayName("Should create with brand when provided")
        void createsWithBrand() {
            // When
            ProductTemplate template = ProductTemplate.create(
                    "Test Product",
                    null,
                    categoryId,
                    uomId,
                    brandId,
                    null,
                    null,
                    false,
                    null
            );

            // Then
            assertThat(template.getBrandId()).isEqualTo(brandId);
        }

        @Test
        @DisplayName("Should create with attributes when provided")
        void createsWithAttributes() {
            // Given
            ProductAttributes attrs = ProductAttributes.of(Map.of(
                    "color", "red",
                    "size", "large"
            ));

            // When
            ProductTemplate template = ProductTemplate.create(
                    "Test Product",
                    null,
                    categoryId,
                    uomId,
                    null,
                    null,
                    null,
                    false,
                    attrs
            );

            // Then
            assertThat(template.getAttributes().getAll()).containsEntry("color", "red");
            assertThat(template.getAttributes().getAll()).containsEntry("size", "large");
        }

        @Test
        @DisplayName("Should trim product name")
        void trimsName() {
            // When
            ProductTemplate template = ProductTemplate.create(
                    "  Trimmed Name  ",
                    null,
                    categoryId,
                    uomId,
                    null,
                    null,
                    null,
                    false,
                    null
            );

            // Then
            assertThat(template.getName()).isEqualTo("Trimmed Name");
        }

        @Test
        @DisplayName("Should throw exception when name is null")
        void throwsExceptionWhenNameIsNull() {
            assertThatThrownBy(() -> ProductTemplate.create(
                    null,
                    null,
                    categoryId,
                    uomId,
                    null,
                    null,
                    null,
                    false,
                    null
            )).isInstanceOf(NullPointerException.class)
              .hasMessageContaining("Name is required");
        }

        @Test
        @DisplayName("Should throw exception when category is null")
        void throwsExceptionWhenCategoryIsNull() {
            assertThatThrownBy(() -> ProductTemplate.create(
                    "Test Product",
                    null,
                    null,
                    uomId,
                    null,
                    null,
                    null,
                    false,
                    null
            )).isInstanceOf(NullPointerException.class)
              .hasMessageContaining("Category is required");
        }

        @Test
        @DisplayName("Should throw exception when unit of measure is null")
        void throwsExceptionWhenUomIsNull() {
            assertThatThrownBy(() -> ProductTemplate.create(
                    "Test Product",
                    null,
                    categoryId,
                    null,
                    null,
                    null,
                    null,
                    false,
                    null
            )).isInstanceOf(NullPointerException.class)
              .hasMessageContaining("Unit of measure is required");
        }

        @Test
        @DisplayName("Should register ProductTemplateCreated event")
        void registersCreatedEvent() {
            // When
            ProductTemplate template = ProductTemplate.create(
                    "Test Product",
                    null,
                    categoryId,
                    uomId,
                    null,
                    null,
                    null,
                    false,
                    null
            );

            // Then
            assertThat(template.getDomainEvents()).hasSize(1);
            assertThat(template.getDomainEvents().get(0))
                    .isInstanceOf(com.store.mgmt.modules.products.domain.event.ProductTemplateCreated.class);
        }
    }

    @Nested
    @DisplayName("updateDetails")
    class UpdateDetails {

        @Test
        @DisplayName("Should update name when changed")
        void updatesName() {
            // Given
            ProductTemplate template = ProductTemplate.create(
                    "Original Name",
                    null,
                    categoryId,
                    uomId,
                    null,
                    null,
                    null,
                    false,
                    null
            );
            template.clearDomainEvents();

            // When
            template.updateDetails(
                    "Updated Name",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // Then
            assertThat(template.getName()).isEqualTo("Updated Name");
            assertThat(template.getDomainEvents()).hasSize(1);
        }

        @Test
        @DisplayName("Should not register event when no fields change")
        void doesNotRegisterEventWhenNoChange() {
            // Given
            ProductTemplate template = ProductTemplate.create(
                    "Original Name",
                    null,
                    categoryId,
                    uomId,
                    null,
                    null,
                    null,
                    false,
                    null
            );
            template.clearDomainEvents();

            // When
            template.updateDetails(
                    "Original Name", // Same name
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // Then
            assertThat(template.getDomainEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Should mark template as deleted")
        void marksAsDeleted() {
            // Given
            ProductTemplate template = ProductTemplate.create(
                    "Test Product",
                    null,
                    categoryId,
                    uomId,
                    null,
                    null,
                    null,
                    false,
                    null
            );
            template.clearDomainEvents();

            // When
            template.delete();

            // Then
            assertThat(template.isDeleted()).isTrue();
            assertThat(template.getDeletedAt()).isNotNull();
            assertThat(template.getDomainEvents()).hasSize(1);
        }

        @Test
        @DisplayName("Should not delete twice")
        void doesNotDeleteTwice() {
            // Given
            ProductTemplate template = ProductTemplate.create(
                    "Test Product",
                    null,
                    categoryId,
                    uomId,
                    null,
                    null,
                    null,
                    false,
                    null
            );
            template.delete();
            template.clearDomainEvents();

            // When
            template.delete();

            // Then
            assertThat(template.getDomainEvents()).isEmpty(); // No new event
        }
    }

    @Nested
    @DisplayName("activate/deactivate")
    class ActivateDeactivate {

        @Test
        @DisplayName("Should deactivate active template")
        void deactivatesTemplate() {
            // Given
            ProductTemplate template = ProductTemplate.create(
                    "Test Product",
                    null,
                    categoryId,
                    uomId,
                    null,
                    null,
                    null,
                    false,
                    null
            );
            assertThat(template.isActive()).isTrue();
            template.clearDomainEvents();

            // When
            template.deactivate();

            // Then
            assertThat(template.isActive()).isFalse();
            assertThat(template.getDomainEvents()).hasSize(1);
        }

        @Test
        @DisplayName("Should activate inactive template")
        void activatesTemplate() {
            // Given
            ProductTemplate template = ProductTemplate.create(
                    "Test Product",
                    null,
                    categoryId,
                    uomId,
                    null,
                    null,
                    null,
                    false,
                    null
            );
            template.deactivate();
            template.clearDomainEvents();

            // When
            template.activate();

            // Then
            assertThat(template.isActive()).isTrue();
            assertThat(template.getDomainEvents()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("variants")
    class Variants {

        @Test
        @DisplayName("Should add variant")
        void addsVariant() {
            // Given
            ProductTemplate template = ProductTemplate.create(
                    "Test Product",
                    null,
                    categoryId,
                    uomId,
                    null,
                    null,
                    null,
                    false,
                    null
            );
            ProductVariantId variantId = ProductVariantId.generate();

            // When
            template.addVariant(variantId);

            // Then
            assertThat(template.hasVariants()).isTrue();
            assertThat(template.getVariantCount()).isEqualTo(1);
            assertThat(template.getVariantIds()).contains(variantId);
        }

        @Test
        @DisplayName("Should not add duplicate variant")
        void doesNotAddDuplicateVariant() {
            // Given
            ProductTemplate template = ProductTemplate.create(
                    "Test Product",
                    null,
                    categoryId,
                    uomId,
                    null,
                    null,
                    null,
                    false,
                    null
            );
            ProductVariantId variantId = ProductVariantId.generate();
            template.addVariant(variantId);

            // When
            template.addVariant(variantId); // Add same variant again

            // Then
            assertThat(template.getVariantCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should remove variant")
        void removesVariant() {
            // Given
            ProductTemplate template = ProductTemplate.create(
                    "Test Product",
                    null,
                    categoryId,
                    uomId,
                    null,
                    null,
                    null,
                    false,
                    null
            );
            ProductVariantId variantId = ProductVariantId.generate();
            template.addVariant(variantId);

            // When
            template.removeVariant(variantId);

            // Then
            assertThat(template.hasVariants()).isFalse();
            assertThat(template.getVariantCount()).isEqualTo(0);
        }
    }
}
