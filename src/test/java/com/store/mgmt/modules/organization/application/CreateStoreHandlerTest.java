package com.store.mgmt.modules.organization.application;

import com.store.mgmt.modules.organization.application.command.CreateStoreCommand;
import com.store.mgmt.modules.organization.application.command.CreateStoreHandler;
import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.modules.organization.domain.exception.DuplicateStoreNameException;
import com.store.mgmt.modules.organization.domain.exception.OrganizationNotFoundException;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.model.StoreStatus;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import com.store.mgmt.testutils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateStoreHandler")
class CreateStoreHandlerTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    private CreateStoreHandler handler;

    private Organization testOrganization;

    @BeforeEach
    void setUp() {
        handler = new CreateStoreHandler(storeRepository, organizationRepository);
        testOrganization = TestDataFactory.createOrganization("Test Org");
    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("Should create store successfully when name is unique within organization")
        void createsStoreWhenNameIsUnique() {
            // Given
            CreateStoreCommand command = new CreateStoreCommand(
                    testOrganization.getId(),
                    "Main Store",
                    "123 Main Street",
                    "US",
                    "store@test.com"
            );

            when(organizationRepository.findById(testOrganization.getId()))
                    .thenReturn(Optional.of(testOrganization));
            when(storeRepository.existsByNameAndOrganizationId("Main Store", testOrganization.getId()))
                    .thenReturn(false);
            when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> {
                Store store = invocation.getArgument(0);
                store.setId(UUID.randomUUID());
                return store;
            });

            // When
            StoreDTO result = handler.handle(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Main Store");
            assertThat(result.getLocation()).isEqualTo("123 Main Street");
            assertThat(result.getCountryCode()).isEqualTo("US");
            assertThat(result.getContactInfo()).isEqualTo("store@test.com");
            assertThat(result.getStatus()).isEqualTo("ACTIVE");
            assertThat(result.getOrganizationId()).isEqualTo(testOrganization.getId());

            ArgumentCaptor<Store> captor = ArgumentCaptor.forClass(Store.class);
            verify(storeRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(StoreStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should throw OrganizationNotFoundException when organization does not exist")
        void throwsExceptionWhenOrganizationNotFound() {
            // Given
            UUID nonExistentOrgId = UUID.randomUUID();
            CreateStoreCommand command = new CreateStoreCommand(
                    nonExistentOrgId,
                    "Store",
                    "Location",
                    "US",
                    null
            );

            when(organizationRepository.findById(nonExistentOrgId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(OrganizationNotFoundException.class);

            verify(storeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw DuplicateStoreNameException when store name exists in organization")
        void throwsExceptionWhenStoreNameExists() {
            // Given
            CreateStoreCommand command = new CreateStoreCommand(
                    testOrganization.getId(),
                    "Existing Store",
                    "Location",
                    "US",
                    null
            );

            when(organizationRepository.findById(testOrganization.getId()))
                    .thenReturn(Optional.of(testOrganization));
            when(storeRepository.existsByNameAndOrganizationId("Existing Store", testOrganization.getId()))
                    .thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(DuplicateStoreNameException.class);

            verify(storeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow same store name in different organizations")
        void allowsSameNameInDifferentOrganizations() {
            // Given
            Organization otherOrg = TestDataFactory.createOrganization("Other Org");

            CreateStoreCommand command = new CreateStoreCommand(
                    otherOrg.getId(),
                    "Main Store", // Same name as might exist in another org
                    "Different Location",
                    "CA",
                    null
            );

            when(organizationRepository.findById(otherOrg.getId()))
                    .thenReturn(Optional.of(otherOrg));
            when(storeRepository.existsByNameAndOrganizationId("Main Store", otherOrg.getId()))
                    .thenReturn(false); // Not a duplicate in THIS org
            when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> {
                Store store = invocation.getArgument(0);
                store.setId(UUID.randomUUID());
                return store;
            });

            // When
            StoreDTO result = handler.handle(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Main Store");
            verify(storeRepository).save(any(Store.class));
        }

        @Test
        @DisplayName("Should create store with ACTIVE status by default")
        void createsStoreWithActiveStatus() {
            // Given
            CreateStoreCommand command = new CreateStoreCommand(
                    testOrganization.getId(),
                    "New Store",
                    null,
                    null,
                    null
            );

            when(organizationRepository.findById(testOrganization.getId()))
                    .thenReturn(Optional.of(testOrganization));
            when(storeRepository.existsByNameAndOrganizationId("New Store", testOrganization.getId()))
                    .thenReturn(false);
            when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> {
                Store store = invocation.getArgument(0);
                store.setId(UUID.randomUUID());
                return store;
            });

            // When
            StoreDTO result = handler.handle(command);

            // Then
            assertThat(result.getStatus()).isEqualTo("ACTIVE");

            ArgumentCaptor<Store> captor = ArgumentCaptor.forClass(Store.class);
            verify(storeRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(StoreStatus.ACTIVE);
        }
    }
}
