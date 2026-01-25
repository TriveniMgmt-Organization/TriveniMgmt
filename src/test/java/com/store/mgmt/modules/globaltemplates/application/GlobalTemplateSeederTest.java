package com.store.mgmt.modules.globaltemplates.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.mgmt.config.GlobalTemplateSeeder;
import com.store.mgmt.modules.globaltemplates.domain.model.GlobalTemplate;
import com.store.mgmt.modules.globaltemplates.domain.repository.GlobalTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalTemplateSeeder")
class GlobalTemplateSeederTest {

    @Mock
    private GlobalTemplateRepository globalTemplateRepository;

    private GlobalTemplateSeeder seeder;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        seeder = new GlobalTemplateSeeder(globalTemplateRepository, objectMapper);
    }

    @Nested
    @DisplayName("seedGlobalTemplates")
    class SeedGlobalTemplates {

        @Test
        @DisplayName("Should skip templates that already exist")
        void skipsExistingTemplates() {
            // Given: All templates already exist
            when(globalTemplateRepository.findByCode(anyString()))
                    .thenReturn(Optional.of(new GlobalTemplate()));

            // When
            int count = seeder.seedGlobalTemplates();

            // Then: No new templates should be saved
            verify(globalTemplateRepository, never()).save(any(GlobalTemplate.class));
            assertThat(count).isZero();
        }

        @Test
        @DisplayName("Should seed new templates when they don't exist")
        void seedsNewTemplates() {
            // Given: No templates exist yet
            when(globalTemplateRepository.findByCode(anyString()))
                    .thenReturn(Optional.empty());
            when(globalTemplateRepository.save(any(GlobalTemplate.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // When
            int count = seeder.seedGlobalTemplates();

            // Then: Templates should be saved
            verify(globalTemplateRepository, atLeastOnce()).save(any(GlobalTemplate.class));
            assertThat(count).isPositive();
        }

        @Test
        @DisplayName("Should correctly parse template metadata from JSON")
        void parsesTemplateMetadata() {
            // Given
            when(globalTemplateRepository.findByCode(anyString()))
                    .thenReturn(Optional.empty());
            ArgumentCaptor<GlobalTemplate> templateCaptor = ArgumentCaptor.forClass(GlobalTemplate.class);
            when(globalTemplateRepository.save(templateCaptor.capture()))
                    .thenAnswer(inv -> inv.getArgument(0));

            // When
            seeder.seedGlobalTemplates();

            // Then: Verify at least one template was saved with correct structure
            assertThat(templateCaptor.getAllValues()).isNotEmpty();
            GlobalTemplate savedTemplate = templateCaptor.getAllValues().get(0);
            assertThat(savedTemplate.getCode()).isNotBlank();
            assertThat(savedTemplate.getName()).isNotBlank();
            assertThat(savedTemplate.getType()).isNotBlank();
            assertThat(savedTemplate.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should normalize entity types in template items")
        void normalizesEntityTypes() {
            // Given
            when(globalTemplateRepository.findByCode(anyString()))
                    .thenReturn(Optional.empty());
            ArgumentCaptor<GlobalTemplate> templateCaptor = ArgumentCaptor.forClass(GlobalTemplate.class);
            when(globalTemplateRepository.save(templateCaptor.capture()))
                    .thenAnswer(inv -> inv.getArgument(0));

            // When
            seeder.seedGlobalTemplates();

            // Then: Entity types should be normalized (SCREAMING_SNAKE_CASE)
            templateCaptor.getAllValues().stream()
                    .flatMap(t -> t.getItems().stream())
                    .forEach(item -> {
                        String entityType = item.getEntityType();
                        assertThat(entityType)
                                .matches("[A-Z_]+")
                                .doesNotContain("-");
                    });
        }
    }

    @Nested
    @DisplayName("Template Item Parsing")
    class TemplateItemParsing {

        @Test
        @DisplayName("Should create template items with correct sort order")
        void createsItemsWithSortOrder() {
            // Given
            when(globalTemplateRepository.findByCode(anyString()))
                    .thenReturn(Optional.empty());
            ArgumentCaptor<GlobalTemplate> templateCaptor = ArgumentCaptor.forClass(GlobalTemplate.class);
            when(globalTemplateRepository.save(templateCaptor.capture()))
                    .thenAnswer(inv -> inv.getArgument(0));

            // When
            seeder.seedGlobalTemplates();

            // Then: Items should have sequential sort orders starting from 0
            templateCaptor.getAllValues().stream()
                    .filter(t -> !t.getItems().isEmpty())
                    .findFirst()
                    .ifPresent(template -> {
                        var sortOrders = template.getItems().stream()
                                .map(item -> item.getSortOrder())
                                .sorted()
                                .toList();

                        // Verify sort orders are sequential starting from 0
                        for (int i = 0; i < sortOrders.size(); i++) {
                            assertThat(sortOrders.get(i)).isEqualTo(i);
                        }
                    });
        }

        @Test
        @DisplayName("Should set correct createdBy for seeded templates")
        void setsCorrectCreatedBy() {
            // Given
            when(globalTemplateRepository.findByCode(anyString()))
                    .thenReturn(Optional.empty());
            ArgumentCaptor<GlobalTemplate> templateCaptor = ArgumentCaptor.forClass(GlobalTemplate.class);
            when(globalTemplateRepository.save(templateCaptor.capture()))
                    .thenAnswer(inv -> inv.getArgument(0));

            // When
            seeder.seedGlobalTemplates();

            // Then
            templateCaptor.getAllValues().forEach(template -> {
                assertThat(template.getCreatedBy()).isEqualTo("global-template-seeder");
                template.getItems().forEach(item ->
                        assertThat(item.getCreatedBy()).isEqualTo("global-template-seeder")
                );
            });
        }
    }
}
