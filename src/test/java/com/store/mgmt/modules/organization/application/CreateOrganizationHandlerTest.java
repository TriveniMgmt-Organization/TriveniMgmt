package com.store.mgmt.modules.organization.application;

import com.store.mgmt.modules.organization.application.command.CreateOrganizationCommand;
import com.store.mgmt.modules.organization.application.command.CreateOrganizationHandler;
import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.modules.organization.domain.exception.DuplicateOrganizationNameException;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateOrganizationHandler")
class CreateOrganizationHandlerTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private TenantContext tenantContext;

    private MockedStatic<TenantContext> tenantContextMock;

    private CreateOrganizationHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateOrganizationHandler(organizationRepository);
        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::current).thenReturn(tenantContext);
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("Should create organization successfully when name is unique")
        void createsOrganizationWhenNameIsUnique() {
            // Given
            CreateOrganizationCommand command = new CreateOrganizationCommand(
                    "Test Organization",
                    "A test organization",
                    "contact@test.org",
                    null
            );

            when(organizationRepository.existsByName("Test Organization")).thenReturn(false);
            when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> {
                Organization org = invocation.getArgument(0);
                org.setId(UUID.randomUUID());
                return org;
            });

            // When
            OrganizationDTO result = handler.handle(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test Organization");
            assertThat(result.getDescription()).isEqualTo("A test organization");
            assertThat(result.getContactInfo()).isEqualTo("contact@test.org");

            ArgumentCaptor<Organization> captor = ArgumentCaptor.forClass(Organization.class);
            verify(organizationRepository).save(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo("Test Organization");
        }

        @Test
        @DisplayName("Should throw DuplicateOrganizationNameException when name already exists")
        void throwsExceptionWhenNameExists() {
            // Given
            CreateOrganizationCommand command = new CreateOrganizationCommand(
                    "Existing Org",
                    "Description",
                    "contact@test.org",
                    null
            );

            when(organizationRepository.existsByName("Existing Org")).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(DuplicateOrganizationNameException.class);

            verify(organizationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow null description and contact info")
        void allowsNullOptionalFields() {
            // Given
            CreateOrganizationCommand command = new CreateOrganizationCommand(
                    "Minimal Org",
                    null,
                    null,
                    null
            );

            when(organizationRepository.existsByName("Minimal Org")).thenReturn(false);
            when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> {
                Organization org = invocation.getArgument(0);
                org.setId(UUID.randomUUID());
                return org;
            });

            // When
            OrganizationDTO result = handler.handle(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Minimal Org");
            assertThat(result.getDescription()).isNull();
        }

        @Test
        @DisplayName("Should set template code when provided")
        void setsTemplateCodeWhenProvided() {
            // Given
            CreateOrganizationCommand command = new CreateOrganizationCommand(
                    "Org With Template",
                    "Description",
                    null,
                    "RETAIL_BASIC"
            );

            when(organizationRepository.existsByName("Org With Template")).thenReturn(false);
            when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> {
                Organization org = invocation.getArgument(0);
                org.setId(UUID.randomUUID());
                return org;
            });

            // When
            OrganizationDTO result = handler.handle(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAppliedTemplateCode()).isEqualTo("RETAIL_BASIC");

            ArgumentCaptor<Organization> captor = ArgumentCaptor.forClass(Organization.class);
            verify(organizationRepository).save(captor.capture());
            assertThat(captor.getValue().getAppliedTemplateCode()).isEqualTo("RETAIL_BASIC");
        }

        @Test
        @DisplayName("Should not set template code when blank")
        void doesNotSetTemplateCodeWhenBlank() {
            // Given
            CreateOrganizationCommand command = new CreateOrganizationCommand(
                    "Org Without Template",
                    "Description",
                    null,
                    "   "
            );

            when(organizationRepository.existsByName("Org Without Template")).thenReturn(false);
            when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> {
                Organization org = invocation.getArgument(0);
                org.setId(UUID.randomUUID());
                return org;
            });

            // When
            OrganizationDTO result = handler.handle(command);

            // Then
            ArgumentCaptor<Organization> captor = ArgumentCaptor.forClass(Organization.class);
            verify(organizationRepository).save(captor.capture());
            assertThat(captor.getValue().getAppliedTemplateCode()).isNull();
        }
    }
}
