package com.store.mgmt.modules.auth.application;

import com.store.mgmt.modules.auth.application.dto.AuthUserDTO;
import com.store.mgmt.modules.auth.application.query.GetCurrentUserHandler;
import com.store.mgmt.modules.auth.application.query.GetCurrentUserQuery;
import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.model.UserOrganizationRole;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import com.store.mgmt.modules.organization.domain.repository.UserOrganizationRoleRepository;
import com.store.mgmt.modules.users.domain.model.Role;
import com.store.mgmt.modules.users.domain.model.User;
import com.store.mgmt.modules.users.domain.repository.UserRepository;
import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaRoleRepository;
import com.store.mgmt.testutils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCurrentUserHandler")
class GetCurrentUserHandlerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private UserOrganizationRoleRepository userOrganizationRoleRepository;
    @Mock
    private JpaRoleRepository roleRepository;

    private GetCurrentUserHandler handler;

    private User testUser;
    private Organization testOrg;
    private Store store1;
    private Store store2;
    private Store store3;
    private Role orgAdminRole;
    private Role cashierRole;

    @BeforeEach
    void setUp() {
        handler = new GetCurrentUserHandler(
                userRepository,
                organizationRepository,
                storeRepository,
                userOrganizationRoleRepository,
                roleRepository
        );

        // Create test data
        testUser = TestDataFactory.createUser("test@example.com");
        testOrg = TestDataFactory.createOrganization("Test Org");
        store1 = TestDataFactory.createStore("Store 1", testOrg);
        store2 = TestDataFactory.createStore("Store 2", testOrg);
        store3 = TestDataFactory.createStore("Store 3", testOrg);

        orgAdminRole = TestDataFactory.createRoleWithPermissions(
                "ORG_ADMIN",
                "ORG_READ", "ORG_WRITE", "STORE_READ", "STORE_WRITE"
        );
        cashierRole = TestDataFactory.createRoleWithPermissions(
                "CASHIER",
                "SALE_READ", "SALE_WRITE"
        );
    }

    private void setupSecurityContext(UUID orgId, UUID storeId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("org_id", orgId.toString());
        claims.put("store_id", storeId != null ? storeId.toString() : null);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                testUser.getEmail(), null, Collections.emptyList()
        );
        auth.setDetails(claims);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Nested
    @DisplayName("Store Visibility")
    class StoreVisibility {

        @Test
        @DisplayName("ORG_ADMIN with org-level role should see all stores in organization")
        void orgAdminSeesAllStores() {
            // Given: User has org-level ORG_ADMIN role (store is null)
            UserOrganizationRole orgLevelRole = TestDataFactory.createOrgLevelRole(testUser, testOrg, orgAdminRole);
            List<UserOrganizationRole> roles = List.of(orgLevelRole);

            setupSecurityContext(testOrg.getId(), null);

            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(userOrganizationRoleRepository.existsByUserIdAndOrganizationId(testUser.getId(), testOrg.getId()))
                    .thenReturn(true);
            when(organizationRepository.findByIdWithStores(testOrg.getId())).thenReturn(Optional.of(testOrg));
            when(userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(testUser.getId()))
                    .thenReturn(roles);
            when(roleRepository.findByIdsWithPermissions(any())).thenReturn(List.of(orgAdminRole));

            // When
            AuthUserDTO result = handler.handle(new GetCurrentUserQuery());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getActiveOrganization()).isNotNull();
            assertThat(result.getActiveOrganization().getStores())
                    .hasSize(3)
                    .extracting(StoreDTO::getName)
                    .containsExactlyInAnyOrder("Store 1", "Store 2", "Store 3");
        }

        @Test
        @DisplayName("User with store-level role should only see assigned stores")
        void storeLevelRoleSeesOnlyAssignedStores() {
            // Given: User has CASHIER role only for Store 1
            UserOrganizationRole storeLevelRole = TestDataFactory.createStoreLevelRole(
                    testUser, testOrg, store1, cashierRole
            );
            List<UserOrganizationRole> roles = List.of(storeLevelRole);

            setupSecurityContext(testOrg.getId(), store1.getId());

            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(userOrganizationRoleRepository.existsByUserIdAndOrganizationId(testUser.getId(), testOrg.getId()))
                    .thenReturn(true);
            when(userOrganizationRoleRepository.existsByUserIdAndStoreId(testUser.getId(), store1.getId()))
                    .thenReturn(true);
            when(organizationRepository.findByIdWithStores(testOrg.getId())).thenReturn(Optional.of(testOrg));
            when(userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(testUser.getId()))
                    .thenReturn(roles);
            when(roleRepository.findByIdsWithPermissions(any())).thenReturn(List.of(cashierRole));
            when(storeRepository.findById(store1.getId())).thenReturn(Optional.of(store1));

            // When
            AuthUserDTO result = handler.handle(new GetCurrentUserQuery());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getActiveOrganization()).isNotNull();
            assertThat(result.getActiveOrganization().getStores())
                    .hasSize(1)
                    .extracting(StoreDTO::getName)
                    .containsExactly("Store 1");
        }

        @Test
        @DisplayName("User with multiple store-level roles should see all assigned stores")
        void multipleStoreLevelRolesSeesAllAssignedStores() {
            // Given: User has CASHIER role for Store 1 and Store 2
            UserOrganizationRole store1Role = TestDataFactory.createStoreLevelRole(
                    testUser, testOrg, store1, cashierRole
            );
            UserOrganizationRole store2Role = TestDataFactory.createStoreLevelRole(
                    testUser, testOrg, store2, cashierRole
            );
            List<UserOrganizationRole> roles = List.of(store1Role, store2Role);

            setupSecurityContext(testOrg.getId(), store1.getId());

            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(userOrganizationRoleRepository.existsByUserIdAndOrganizationId(testUser.getId(), testOrg.getId()))
                    .thenReturn(true);
            when(userOrganizationRoleRepository.existsByUserIdAndStoreId(testUser.getId(), store1.getId()))
                    .thenReturn(true);
            when(organizationRepository.findByIdWithStores(testOrg.getId())).thenReturn(Optional.of(testOrg));
            when(userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(testUser.getId()))
                    .thenReturn(roles);
            when(roleRepository.findByIdsWithPermissions(any())).thenReturn(List.of(cashierRole));
            when(storeRepository.findById(store1.getId())).thenReturn(Optional.of(store1));

            // When
            AuthUserDTO result = handler.handle(new GetCurrentUserQuery());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getActiveOrganization()).isNotNull();
            assertThat(result.getActiveOrganization().getStores())
                    .hasSize(2)
                    .extracting(StoreDTO::getName)
                    .containsExactlyInAnyOrder("Store 1", "Store 2");
        }

        @Test
        @DisplayName("User with both org-level and store-level roles should see all stores")
        void mixedRolesSeesAllStores() {
            // Given: User has ORG_ADMIN (org-level) and CASHIER (store-level for Store 1)
            UserOrganizationRole orgLevelRole = TestDataFactory.createOrgLevelRole(testUser, testOrg, orgAdminRole);
            UserOrganizationRole storeLevelRole = TestDataFactory.createStoreLevelRole(
                    testUser, testOrg, store1, cashierRole
            );
            List<UserOrganizationRole> roles = List.of(orgLevelRole, storeLevelRole);

            setupSecurityContext(testOrg.getId(), null);

            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(userOrganizationRoleRepository.existsByUserIdAndOrganizationId(testUser.getId(), testOrg.getId()))
                    .thenReturn(true);
            when(organizationRepository.findByIdWithStores(testOrg.getId())).thenReturn(Optional.of(testOrg));
            when(userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(testUser.getId()))
                    .thenReturn(roles);
            when(roleRepository.findByIdsWithPermissions(any())).thenReturn(List.of(orgAdminRole, cashierRole));

            // When
            AuthUserDTO result = handler.handle(new GetCurrentUserQuery());

            // Then: Should see all stores because of org-level role
            assertThat(result).isNotNull();
            assertThat(result.getActiveOrganization()).isNotNull();
            assertThat(result.getActiveOrganization().getStores())
                    .hasSize(3)
                    .extracting(StoreDTO::getName)
                    .containsExactlyInAnyOrder("Store 1", "Store 2", "Store 3");
        }
    }

    @Nested
    @DisplayName("Permissions")
    class Permissions {

        @Test
        @DisplayName("Should return permissions from user's role")
        void returnsPermissionsFromRole() {
            // Given
            UserOrganizationRole orgLevelRole = TestDataFactory.createOrgLevelRole(testUser, testOrg, orgAdminRole);
            List<UserOrganizationRole> roles = List.of(orgLevelRole);

            setupSecurityContext(testOrg.getId(), null);

            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(userOrganizationRoleRepository.existsByUserIdAndOrganizationId(testUser.getId(), testOrg.getId()))
                    .thenReturn(true);
            when(organizationRepository.findByIdWithStores(testOrg.getId())).thenReturn(Optional.of(testOrg));
            when(userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(testUser.getId()))
                    .thenReturn(roles);
            when(roleRepository.findByIdsWithPermissions(any())).thenReturn(List.of(orgAdminRole));

            // When
            AuthUserDTO result = handler.handle(new GetCurrentUserQuery());

            // Then
            assertThat(result.getPermissions())
                    .containsExactlyInAnyOrder("ORG_READ", "ORG_WRITE", "STORE_READ", "STORE_WRITE");
            assertThat(result.getRoles())
                    .containsExactly("ORG_ADMIN");
        }
    }
}
