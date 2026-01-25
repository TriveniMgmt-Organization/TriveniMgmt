package com.store.mgmt.modules.globaltemplates.application;

import com.store.mgmt.modules.globaltemplates.domain.model.GlobalTemplate;
import com.store.mgmt.modules.globaltemplates.domain.model.GlobalTemplateItem;
import com.store.mgmt.modules.globaltemplates.domain.repository.GlobalTemplateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Global Templates Repository Tests")
@org.junit.jupiter.api.Disabled("Requires PostgreSQL for JSONB support - enable with Testcontainers")
class GlobalTemplatesIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GlobalTemplateRepository globalTemplateRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private GlobalTemplate testTemplate;

    @BeforeEach
    void setUp() {
        // Create a test template
        testTemplate = new GlobalTemplate();
        testTemplate.setCode("TEST_TEMPLATE");
        testTemplate.setName("Test Template");
        testTemplate.setType("RETAIL");
        testTemplate.setDescription("A test template");
        testTemplate.setIsActive(true);
        testTemplate.setVersion(1);
        testTemplate.setCreatedAt(LocalDateTime.now());
        testTemplate.setCreatedBy("test");

        // Add items
        Set<GlobalTemplateItem> items = new LinkedHashSet<>();

        GlobalTemplateItem brandItem = new GlobalTemplateItem();
        brandItem.setTemplate(testTemplate);
        brandItem.setEntityType("BRAND");
        ObjectNode brandData = objectMapper.createObjectNode();
        brandData.put("name", "Test Brand");
        brandItem.setData(brandData);
        brandItem.setSortOrder(0);
        brandItem.setCreatedAt(LocalDateTime.now());
        brandItem.setCreatedBy("test");
        items.add(brandItem);

        GlobalTemplateItem categoryItem = new GlobalTemplateItem();
        categoryItem.setTemplate(testTemplate);
        categoryItem.setEntityType("CATEGORY");
        ObjectNode categoryData = objectMapper.createObjectNode();
        categoryData.put("code", "TEST");
        categoryData.put("name", "Test Category");
        categoryItem.setData(categoryData);
        categoryItem.setSortOrder(1);
        categoryItem.setCreatedAt(LocalDateTime.now());
        categoryItem.setCreatedBy("test");
        items.add(categoryItem);

        testTemplate.setItems(items);
        entityManager.persist(testTemplate);
        entityManager.flush();
    }

    @Nested
    @DisplayName("findByCode")
    class FindByCode {

        @Test
        @DisplayName("Should find template by code")
        void findsTemplateByCode() {
            Optional<GlobalTemplate> result = globalTemplateRepository.findByCode("TEST_TEMPLATE");

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Test Template");
            assertThat(result.get().getType()).isEqualTo("RETAIL");
        }

        @Test
        @DisplayName("Should return empty for non-existent code")
        void returnsEmptyForNonExistentCode() {
            Optional<GlobalTemplate> result = globalTemplateRepository.findByCode("NON_EXISTENT");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllActive")
    class FindActiveTemplates {

        @Test
        @DisplayName("Should return only active templates")
        void returnsOnlyActiveTemplates() {
            // Create an inactive template
            GlobalTemplate inactiveTemplate = new GlobalTemplate();
            inactiveTemplate.setCode("INACTIVE_TEMPLATE");
            inactiveTemplate.setName("Inactive Template");
            inactiveTemplate.setType("RETAIL");
            inactiveTemplate.setIsActive(false);
            inactiveTemplate.setVersion(1);
            inactiveTemplate.setCreatedAt(LocalDateTime.now());
            inactiveTemplate.setCreatedBy("test");
            inactiveTemplate.setItems(new LinkedHashSet<>());
            entityManager.persist(inactiveTemplate);
            entityManager.flush();

            List<GlobalTemplate> activeTemplates = globalTemplateRepository.findAllActive();

            assertThat(activeTemplates)
                    .extracting(GlobalTemplate::getCode)
                    .contains("TEST_TEMPLATE")
                    .doesNotContain("INACTIVE_TEMPLATE");
        }
    }

    @Nested
    @DisplayName("findByType")
    class FindByType {

        @Test
        @DisplayName("Should find templates by type")
        void findsTemplatesByType() {
            List<GlobalTemplate> retailTemplates = globalTemplateRepository.findByType("RETAIL");

            assertThat(retailTemplates)
                    .isNotEmpty()
                    .allMatch(t -> "RETAIL".equals(t.getType()));
        }

        @Test
        @DisplayName("Should return empty list for unknown type")
        void returnsEmptyForUnknownType() {
            List<GlobalTemplate> templates = globalTemplateRepository.findByType("UNKNOWN");

            assertThat(templates).isEmpty();
        }
    }

    @Nested
    @DisplayName("Template with Items")
    class TemplateWithItems {

        @Test
        @DisplayName("Should persist template items correctly")
        void persistsTemplateItems() {
            Optional<GlobalTemplate> result = globalTemplateRepository.findByCode("TEST_TEMPLATE");

            assertThat(result).isPresent();
            GlobalTemplate template = result.get();
            assertThat(template.getItems()).hasSize(2);
            assertThat(template.getItems())
                    .extracting(GlobalTemplateItem::getEntityType)
                    .containsExactlyInAnyOrder("BRAND", "CATEGORY");
        }

        @Test
        @DisplayName("Should maintain sort order of items")
        void maintainsSortOrder() {
            Optional<GlobalTemplate> result = globalTemplateRepository.findByCode("TEST_TEMPLATE");

            assertThat(result).isPresent();
            List<GlobalTemplateItem> sortedItems = result.get().getItems().stream()
                    .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                    .toList();

            assertThat(sortedItems.get(0).getEntityType()).isEqualTo("BRAND");
            assertThat(sortedItems.get(1).getEntityType()).isEqualTo("CATEGORY");
        }

        @Test
        @DisplayName("Should store JSON data correctly")
        void storesJsonDataCorrectly() {
            Optional<GlobalTemplate> result = globalTemplateRepository.findByCode("TEST_TEMPLATE");

            assertThat(result).isPresent();
            GlobalTemplateItem brandItem = result.get().getItems().stream()
                    .filter(i -> "BRAND".equals(i.getEntityType()))
                    .findFirst()
                    .orElseThrow();

            assertThat(brandItem.getData().get("name").asText()).isEqualTo("Test Brand");
        }
    }
}
