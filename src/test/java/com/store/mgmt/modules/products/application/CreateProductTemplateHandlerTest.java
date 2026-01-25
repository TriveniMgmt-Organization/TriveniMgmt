package com.store.mgmt.modules.products.application;

import com.store.mgmt.modules.products.application.command.CreateProductTemplateCommand;
import com.store.mgmt.modules.products.application.command.CreateProductTemplateHandler;
import com.store.mgmt.modules.products.application.dto.ProductTemplateDTO;
import com.store.mgmt.modules.products.domain.model.*;
import com.store.mgmt.modules.products.domain.repository.ProductTemplateRepository;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateProductTemplateHandler")
class CreateProductTemplateHandlerTest {

    @Mock
    private ProductTemplateRepository templateRepository;

    @Mock
    private TenantContext tenantContext;

    private MockedStatic<TenantContext> tenantContextMock;

    private CreateProductTemplateHandler handler;

    private UUID organizationId;
    private UUID categoryId;
    private UUID uomId;
    private UUID brandId;

    @BeforeEach
    void setUp() {
        handler = new CreateProductTemplateHandler(templateRepository);

        organizationId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        uomId = UUID.randomUUID();
        brandId = UUID.randomUUID();

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
        @DisplayName("Should create product template with all fields")
        void createsTemplateWithAllFields() {
            // Given
            CreateProductTemplateCommand command = new CreateProductTemplateCommand(
                    "Test Product",
                    "Product description",
                    categoryId,
                    uomId,
                    brandId,
                    "https://example.com/image.jpg",
                    20,
                    true,
                    Map.of("color", "blue", "size", "medium")
            );

            when(templateRepository.save(any(ProductTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            ProductTemplateDTO result = handler.handle(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test Product");
            assertThat(result.getDescription()).isEqualTo("Product description");
            assertThat(result.getCategoryId()).isEqualTo(categoryId);
            assertThat(result.getUnitOfMeasureId()).isEqualTo(uomId);
            assertThat(result.getBrandId()).isEqualTo(brandId);
            assertThat(result.getImageUrl()).isEqualTo("https://example.com/image.jpg");
            assertThat(result.getReorderPoint()).isEqualTo(20);
            assertThat(result.isRequiresExpiry()).isTrue();
            assertThat(result.isActive()).isTrue();
            assertThat(result.getAttributes()).containsEntry("color", "blue");

            verify(templateRepository).save(any(ProductTemplate.class));
        }

        @Test
        @DisplayName("Should create product template with minimal required fields")
        void createsTemplateWithMinimalFields() {
            // Given
            CreateProductTemplateCommand command = new CreateProductTemplateCommand(
                    "Simple Product",
                    null,
                    categoryId,
                    uomId,
                    null,
                    null,
                    null,
                    false,
                    null
            );

            when(templateRepository.save(any(ProductTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            ProductTemplateDTO result = handler.handle(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Simple Product");
            assertThat(result.getDescription()).isNull();
            assertThat(result.getBrandId()).isNull();
            assertThat(result.getReorderPoint()).isEqualTo(10); // Default
            assertThat(result.isRequiresExpiry()).isFalse();
        }

        @Test
        @DisplayName("Should save template with correct organization ID from tenant context")
        void savesWithCorrectOrganizationId() {
            // Given
            CreateProductTemplateCommand command = new CreateProductTemplateCommand(
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

            ArgumentCaptor<ProductTemplate> captor = ArgumentCaptor.forClass(ProductTemplate.class);
            when(templateRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            // When
            handler.handle(command);

            // Then
            ProductTemplate savedTemplate = captor.getValue();
            assertThat(savedTemplate.getOrganizationId().getValue()).isEqualTo(organizationId);
        }

        @Test
        @DisplayName("Should return DTO with generated ID")
        void returnsDtoWithGeneratedId() {
            // Given
            CreateProductTemplateCommand command = new CreateProductTemplateCommand(
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

            when(templateRepository.save(any(ProductTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            ProductTemplateDTO result = handler.handle(command);

            // Then
            assertThat(result.getId()).isNotNull();
        }
    }
}
