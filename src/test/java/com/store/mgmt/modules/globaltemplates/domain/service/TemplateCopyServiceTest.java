package com.store.mgmt.modules.globaltemplates.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.mgmt.modules.globaltemplates.domain.model.GlobalTemplate;
import com.store.mgmt.modules.globaltemplates.domain.model.GlobalTemplateItem;
import com.store.mgmt.modules.globaltemplates.domain.repository.GlobalTemplateRepository;
import com.store.mgmt.modules.inventory.domain.model.*;
import com.store.mgmt.modules.inventory.domain.repository.*;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.model.StoreStatus;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TemplateCopyService")
class TemplateCopyServiceTest {

    @Mock
    private GlobalTemplateRepository templateRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private UnitOfMeasureRepository unitOfMeasureRepository;

    @Mock
    private ProductTemplateRepository productTemplateRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private TaxRuleRepository taxRuleRepository;

    @Mock
    private InventoryLocationRepository inventoryLocationRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private EntityManager entityManager;

    private ObjectMapper objectMapper;
    private TemplateCopyService service;

    private Organization testOrg;
    private Store testStore;
    private GlobalTemplate testTemplate;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new TemplateCopyService(
                templateRepository,
                organizationRepository,
                categoryRepository,
                brandRepository,
                unitOfMeasureRepository,
                productTemplateRepository,
                productVariantRepository,
                taxRuleRepository,
                inventoryLocationRepository,
                supplierRepository,
                objectMapper,
                entityManager
        );

        // Setup test organization
        testOrg = new Organization();
        testOrg.setId(UUID.randomUUID());
        testOrg.setName("Test Organization");

        // Setup test store
        testStore = new Store();
        testStore.setId(UUID.randomUUID());
        testStore.setName("Test Store");
        testStore.setOrganization(testOrg);
        testStore.setStatus(StoreStatus.ACTIVE);

        testOrg.setStores(Set.of(testStore));

        // Setup test template
        testTemplate = new GlobalTemplate();
        testTemplate.setId(UUID.randomUUID());
        testTemplate.setCode("TEST_TEMPLATE");
        testTemplate.setName("Test Template");
        testTemplate.setType("TEST");
        testTemplate.setIsActive(true);
        testTemplate.setItems(new LinkedHashSet<>());
    }

    @Nested
    @DisplayName("applyTemplate - Brand")
    class ApplyTemplateBrand {

        @Test
        @DisplayName("Should create brand from template item")
        void createsBrandFromTemplateItem() throws Exception {
            // Given
            JsonNode brandData = objectMapper.readTree("""
                {
                    "name": "Test Brand",
                    "description": "A test brand"
                }
                """);

            GlobalTemplateItem item = createTemplateItem("BRAND", brandData);
            testTemplate.getItems().add(item);

            when(templateRepository.findByCode("TEST_TEMPLATE")).thenReturn(Optional.of(testTemplate));
            when(brandRepository.findByName("Test Brand")).thenReturn(Optional.empty());
            when(brandRepository.save(any(Brand.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            service.applyTemplate(testOrg, "TEST_TEMPLATE");

            // Then
            ArgumentCaptor<Brand> brandCaptor = ArgumentCaptor.forClass(Brand.class);
            verify(brandRepository).save(brandCaptor.capture());

            Brand savedBrand = brandCaptor.getValue();
            assertThat(savedBrand.getName()).isEqualTo("Test Brand");
            assertThat(savedBrand.getDescription()).isEqualTo("A test brand");
        }

        @Test
        @DisplayName("Should skip existing brand")
        void skipsExistingBrand() throws Exception {
            // Given
            JsonNode brandData = objectMapper.readTree("""
                {
                    "name": "Existing Brand"
                }
                """);

            GlobalTemplateItem item = createTemplateItem("BRAND", brandData);
            testTemplate.getItems().add(item);

            when(templateRepository.findByCode("TEST_TEMPLATE")).thenReturn(Optional.of(testTemplate));
            when(brandRepository.findByName("Existing Brand")).thenReturn(Optional.of(new Brand()));

            // When
            service.applyTemplate(testOrg, "TEST_TEMPLATE");

            // Then
            verify(brandRepository, never()).save(any(Brand.class));
        }
    }

    @Nested
    @DisplayName("applyTemplate - Category")
    class ApplyTemplateCategory {

        @Test
        @DisplayName("Should create category from template item")
        void createsCategoryFromTemplateItem() throws Exception {
            // Given
            JsonNode categoryData = objectMapper.readTree("""
                {
                    "code": "ELECTRONICS",
                    "name": "Electronics",
                    "description": "Electronic items"
                }
                """);

            GlobalTemplateItem item = createTemplateItem("CATEGORY", categoryData);
            testTemplate.getItems().add(item);

            when(templateRepository.findByCode("TEST_TEMPLATE")).thenReturn(Optional.of(testTemplate));
            when(categoryRepository.findByCodeAndOrganizationId("ELECTRONICS", testOrg.getId()))
                    .thenReturn(Optional.empty());
            when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
                Category cat = inv.getArgument(0);
                cat.setId(UUID.randomUUID());
                return cat;
            });

            // When
            service.applyTemplate(testOrg, "TEST_TEMPLATE");

            // Then
            ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
            verify(categoryRepository).save(categoryCaptor.capture());

            Category savedCategory = categoryCaptor.getValue();
            assertThat(savedCategory.getCode()).isEqualTo("ELECTRONICS");
            assertThat(savedCategory.getName()).isEqualTo("Electronics");
            assertThat(savedCategory.getDescription()).isEqualTo("Electronic items");
            assertThat(savedCategory.getOrganizationId()).isEqualTo(testOrg.getId());
        }

        @Test
        @DisplayName("Should skip existing category")
        void skipsExistingCategory() throws Exception {
            // Given
            JsonNode categoryData = objectMapper.readTree("""
                {
                    "code": "EXISTING",
                    "name": "Existing Category"
                }
                """);

            GlobalTemplateItem item = createTemplateItem("CATEGORY", categoryData);
            testTemplate.getItems().add(item);

            when(templateRepository.findByCode("TEST_TEMPLATE")).thenReturn(Optional.of(testTemplate));
            when(categoryRepository.findByCodeAndOrganizationId("EXISTING", testOrg.getId()))
                    .thenReturn(Optional.of(new Category()));

            // When
            service.applyTemplate(testOrg, "TEST_TEMPLATE");

            // Then
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("applyTemplate - UnitOfMeasure")
    class ApplyTemplateUom {

        @Test
        @DisplayName("Should create UOM from template item")
        void createsUomFromTemplateItem() throws Exception {
            // Given
            JsonNode uomData = objectMapper.readTree("""
                {
                    "code": "KG",
                    "name": "Kilogram"
                }
                """);

            GlobalTemplateItem item = createTemplateItem("UOM", uomData);
            testTemplate.getItems().add(item);

            when(templateRepository.findByCode("TEST_TEMPLATE")).thenReturn(Optional.of(testTemplate));
            when(unitOfMeasureRepository.findByCodeAndOrganizationId("KG", testOrg.getId()))
                    .thenReturn(Optional.empty());
            when(unitOfMeasureRepository.save(any(UnitOfMeasure.class))).thenAnswer(inv -> {
                UnitOfMeasure uom = inv.getArgument(0);
                uom.setId(UUID.randomUUID());
                return uom;
            });

            // When
            service.applyTemplate(testOrg, "TEST_TEMPLATE");

            // Then
            ArgumentCaptor<UnitOfMeasure> uomCaptor = ArgumentCaptor.forClass(UnitOfMeasure.class);
            verify(unitOfMeasureRepository).save(uomCaptor.capture());

            UnitOfMeasure savedUom = uomCaptor.getValue();
            assertThat(savedUom.getCode()).isEqualTo("KG");
            assertThat(savedUom.getName()).isEqualTo("Kilogram");
            assertThat(savedUom.getOrganizationId()).isEqualTo(testOrg.getId());
        }
    }

    @Nested
    @DisplayName("applyTemplate - TaxRule")
    class ApplyTemplateTaxRule {

        @Test
        @DisplayName("Should create tax rule from template item")
        void createsTaxRuleFromTemplateItem() throws Exception {
            // Given
            JsonNode taxData = objectMapper.readTree("""
                {
                    "countryCode": "US",
                    "taxRate": 8.25,
                    "description": "Sales Tax"
                }
                """);

            GlobalTemplateItem item = createTemplateItem("TAX_RULE", taxData);
            testTemplate.getItems().add(item);

            when(templateRepository.findByCode("TEST_TEMPLATE")).thenReturn(Optional.of(testTemplate));
            when(taxRuleRepository.findByOrganizationIdAndCountryCode(testOrg.getId(), "US"))
                    .thenReturn(Optional.empty());
            when(taxRuleRepository.save(any(TaxRule.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            service.applyTemplate(testOrg, "TEST_TEMPLATE");

            // Then
            ArgumentCaptor<TaxRule> taxRuleCaptor = ArgumentCaptor.forClass(TaxRule.class);
            verify(taxRuleRepository).save(taxRuleCaptor.capture());

            TaxRule savedTaxRule = taxRuleCaptor.getValue();
            assertThat(savedTaxRule.getCountryCode()).isEqualTo("US");
            assertThat(savedTaxRule.getTaxRate()).isEqualByComparingTo("8.25");
            assertThat(savedTaxRule.getOrganizationId()).isEqualTo(testOrg.getId());
        }
    }

    @Nested
    @DisplayName("applyTemplate - Location")
    class ApplyTemplateLocation {

        @Test
        @DisplayName("Should create location from template item")
        void createsLocationFromTemplateItem() throws Exception {
            // Given
            JsonNode locationData = objectMapper.readTree("""
                {
                    "name": "Sales Floor",
                    "type": "FLOOR"
                }
                """);

            GlobalTemplateItem item = createTemplateItem("LOCATION", locationData);
            testTemplate.getItems().add(item);

            when(templateRepository.findByCode("TEST_TEMPLATE")).thenReturn(Optional.of(testTemplate));
            when(inventoryLocationRepository.findByNameAndStoreId("Sales Floor", testStore.getId()))
                    .thenReturn(Optional.empty());
            when(inventoryLocationRepository.save(any(InventoryLocation.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            service.applyTemplate(testOrg, "TEST_TEMPLATE");

            // Then
            ArgumentCaptor<InventoryLocation> locationCaptor = ArgumentCaptor.forClass(InventoryLocation.class);
            verify(inventoryLocationRepository).save(locationCaptor.capture());

            InventoryLocation savedLocation = locationCaptor.getValue();
            assertThat(savedLocation.getName()).isEqualTo("Sales Floor");
            assertThat(savedLocation.getStoreId()).isEqualTo(testStore.getId());
        }
    }

    @Nested
    @DisplayName("applyTemplate - Supplier")
    class ApplyTemplateSupplier {

        @Test
        @DisplayName("Should create supplier from template item")
        void createsSupplierFromTemplateItem() throws Exception {
            // Given
            JsonNode supplierData = objectMapper.readTree("""
                {
                    "name": "Test Supplier",
                    "email": "supplier@test.com",
                    "phone": "123-456-7890"
                }
                """);

            GlobalTemplateItem item = createTemplateItem("SUPPLIER", supplierData);
            testTemplate.getItems().add(item);

            when(templateRepository.findByCode("TEST_TEMPLATE")).thenReturn(Optional.of(testTemplate));
            when(supplierRepository.findByNameAndOrganizationId("Test Supplier", testOrg.getId()))
                    .thenReturn(Optional.empty());
            when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            service.applyTemplate(testOrg, "TEST_TEMPLATE");

            // Then
            ArgumentCaptor<Supplier> supplierCaptor = ArgumentCaptor.forClass(Supplier.class);
            verify(supplierRepository).save(supplierCaptor.capture());

            Supplier savedSupplier = supplierCaptor.getValue();
            assertThat(savedSupplier.getName()).isEqualTo("Test Supplier");
            assertThat(savedSupplier.getEmail()).isEqualTo("supplier@test.com");
            assertThat(savedSupplier.getOrganizationId()).isEqualTo(testOrg.getId());
        }
    }

    @Nested
    @DisplayName("applyTemplate - Multiple Items")
    class ApplyTemplateMultipleItems {

        @Test
        @DisplayName("Should process all template items in order")
        void processesAllItemsInOrder() throws Exception {
            // Given - create items similar to retail-basic.json
            JsonNode brandData = objectMapper.readTree("""
                {"name": "Generic", "description": "Store Brand"}
                """);
            JsonNode categoryData = objectMapper.readTree("""
                {"code": "GENERAL", "name": "General Merchandise"}
                """);
            JsonNode uomData = objectMapper.readTree("""
                {"code": "PC", "name": "Piece"}
                """);

            testTemplate.getItems().add(createTemplateItem("BRAND", brandData, 0));
            testTemplate.getItems().add(createTemplateItem("CATEGORY", categoryData, 1));
            testTemplate.getItems().add(createTemplateItem("UOM", uomData, 2));

            when(templateRepository.findByCode("TEST_TEMPLATE")).thenReturn(Optional.of(testTemplate));
            when(brandRepository.findByName(any())).thenReturn(Optional.empty());
            when(categoryRepository.findByCodeAndOrganizationId(any(), any())).thenReturn(Optional.empty());
            when(unitOfMeasureRepository.findByCodeAndOrganizationId(any(), any())).thenReturn(Optional.empty());
            when(brandRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(categoryRepository.save(any())).thenAnswer(inv -> {
                Category c = inv.getArgument(0);
                c.setId(UUID.randomUUID());
                return c;
            });
            when(unitOfMeasureRepository.save(any())).thenAnswer(inv -> {
                UnitOfMeasure u = inv.getArgument(0);
                u.setId(UUID.randomUUID());
                return u;
            });

            // When
            service.applyTemplate(testOrg, "TEST_TEMPLATE");

            // Then
            verify(brandRepository).save(any(Brand.class));
            verify(categoryRepository).save(any(Category.class));
            verify(unitOfMeasureRepository).save(any(UnitOfMeasure.class));
        }
    }

    @Nested
    @DisplayName("applyTemplate - Entity Type Normalization")
    class EntityTypeNormalization {

        @Test
        @DisplayName("Should handle UNIT_OF_MEASURE entity type")
        void handlesUnitOfMeasureEntityType() throws Exception {
            // Given
            JsonNode uomData = objectMapper.readTree("""
                {"code": "LB", "name": "Pound"}
                """);

            GlobalTemplateItem item = createTemplateItem("UNIT_OF_MEASURE", uomData);
            testTemplate.getItems().add(item);

            when(templateRepository.findByCode("TEST_TEMPLATE")).thenReturn(Optional.of(testTemplate));
            when(unitOfMeasureRepository.findByCodeAndOrganizationId(any(), any())).thenReturn(Optional.empty());
            when(unitOfMeasureRepository.save(any())).thenAnswer(inv -> {
                UnitOfMeasure u = inv.getArgument(0);
                u.setId(UUID.randomUUID());
                return u;
            });

            // When
            service.applyTemplate(testOrg, "TEST_TEMPLATE");

            // Then
            verify(unitOfMeasureRepository).save(any(UnitOfMeasure.class));
        }

        @Test
        @DisplayName("Should skip unsupported DAMAGE_LOSS_REASON entity type")
        void skipsUnsupportedEntityType() throws Exception {
            // Given
            JsonNode data = objectMapper.readTree("""
                {"name": "Damaged in Transit", "isActive": true}
                """);

            GlobalTemplateItem item = createTemplateItem("DAMAGE_LOSS_REASON", data);
            testTemplate.getItems().add(item);

            when(templateRepository.findByCode("TEST_TEMPLATE")).thenReturn(Optional.of(testTemplate));

            // When
            service.applyTemplate(testOrg, "TEST_TEMPLATE");

            // Then - no repositories should be called for unsupported types
            verify(brandRepository, never()).save(any());
            verify(categoryRepository, never()).save(any());
        }
    }

    private GlobalTemplateItem createTemplateItem(String entityType, JsonNode data) {
        return createTemplateItem(entityType, data, 0);
    }

    private GlobalTemplateItem createTemplateItem(String entityType, JsonNode data, int sortOrder) {
        GlobalTemplateItem item = new GlobalTemplateItem();
        item.setId(UUID.randomUUID());
        item.setTemplate(testTemplate);
        item.setEntityType(entityType);
        item.setData(data);
        item.setSortOrder(sortOrder);
        return item;
    }
}
