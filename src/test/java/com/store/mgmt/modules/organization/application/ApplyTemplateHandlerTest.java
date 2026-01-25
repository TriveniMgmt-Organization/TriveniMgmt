package com.store.mgmt.modules.organization.application;

import com.store.mgmt.modules.globaltemplates.domain.service.TemplateCopyService;
import com.store.mgmt.modules.organization.application.command.ApplyTemplateCommand;
import com.store.mgmt.modules.organization.application.command.ApplyTemplateHandler;
import com.store.mgmt.modules.organization.domain.exception.OrganizationNotFoundException;
import com.store.mgmt.modules.organization.domain.exception.TemplateAlreadyAppliedException;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.model.StoreStatus;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplyTemplateHandler (Organization Module)")
class ApplyTemplateHandlerTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private TemplateCopyService templateCopyService;

    private ApplyTemplateHandler handler;

    private Organization testOrg;
    private Store testStore;
    private UUID organizationId;

    @BeforeEach
    void setUp() {
        handler = new ApplyTemplateHandler(organizationRepository, templateCopyService);

        organizationId = UUID.randomUUID();

        testOrg = new Organization();
        testOrg.setId(organizationId);
        testOrg.setName("Test Organization");
        testOrg.setAppliedTemplateCode(null);

        testStore = new Store();
        testStore.setId(UUID.randomUUID());
        testStore.setName("Test Store");
        testStore.setOrganization(testOrg);
        testStore.setStatus(StoreStatus.ACTIVE);

        testOrg.setStores(Set.of(testStore));
    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("Should apply template and copy items to organization")
        void appliesTemplateAndCopiesItems() {
            // Given
            ApplyTemplateCommand cmd = new ApplyTemplateCommand(organizationId, "RETAIL_BASIC");

            when(organizationRepository.findByIdWithStores(organizationId)).thenReturn(Optional.of(testOrg));
            when(organizationRepository.save(any(Organization.class))).thenReturn(testOrg);

            // When
            handler.handle(cmd);

            // Then
            // Verify template items are copied BEFORE marking as applied
            InOrder inOrder = inOrder(templateCopyService, organizationRepository);
            inOrder.verify(templateCopyService).applyTemplate(testOrg, "RETAIL_BASIC");
            inOrder.verify(organizationRepository).save(testOrg);

            assertThat(testOrg.getAppliedTemplateCode()).isEqualTo("RETAIL_BASIC");
        }

        @Test
        @DisplayName("Should throw OrganizationNotFoundException when organization not found")
        void throwsWhenOrganizationNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            ApplyTemplateCommand cmd = new ApplyTemplateCommand(nonExistentId, "RETAIL_BASIC");

            when(organizationRepository.findByIdWithStores(nonExistentId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> handler.handle(cmd))
                    .isInstanceOf(OrganizationNotFoundException.class);

            verify(templateCopyService, never()).applyTemplate(any(), any());
        }

        @Test
        @DisplayName("Should throw TemplateAlreadyAppliedException when template already applied")
        void throwsWhenTemplateAlreadyApplied() {
            // Given
            testOrg.setAppliedTemplateCode("GROCERY_PRO"); // Already has a template
            ApplyTemplateCommand cmd = new ApplyTemplateCommand(organizationId, "RETAIL_BASIC");

            when(organizationRepository.findByIdWithStores(organizationId)).thenReturn(Optional.of(testOrg));

            // When/Then
            assertThatThrownBy(() -> handler.handle(cmd))
                    .isInstanceOf(TemplateAlreadyAppliedException.class);

            verify(templateCopyService, never()).applyTemplate(any(), any());
            verify(organizationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fetch organization with stores for location copying")
        void fetchesOrganizationWithStores() {
            // Given
            ApplyTemplateCommand cmd = new ApplyTemplateCommand(organizationId, "RETAIL_BASIC");

            when(organizationRepository.findByIdWithStores(organizationId)).thenReturn(Optional.of(testOrg));
            when(organizationRepository.save(any(Organization.class))).thenReturn(testOrg);

            // When
            handler.handle(cmd);

            // Then
            verify(organizationRepository).findByIdWithStores(organizationId);
            verify(organizationRepository, never()).findById(organizationId);
        }
    }
}
