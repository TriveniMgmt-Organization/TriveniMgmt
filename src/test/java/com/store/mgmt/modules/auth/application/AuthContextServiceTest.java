package com.store.mgmt.modules.auth.application;

import com.store.mgmt.modules.auth.application.dto.AuthUserDTO;
import com.store.mgmt.modules.auth.application.service.AuthContextService;
import com.store.mgmt.modules.auth.application.service.AuthContextService.ActiveContext;
import com.store.mgmt.modules.auth.domain.repository.RefreshTokenRepository;
import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.model.UserOrganizationRole;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.modules.organization.domain.repository.UserOrganizationRoleRepository;
import com.store.mgmt.modules.users.domain.model.Role;
import com.store.mgmt.modules.users.domain.model.User;
import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaRoleRepository;
import com.store.mgmt.shared.infrastructure.security.JWTService;
import com.store.mgmt.testutils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthContextService")
class AuthContextServiceTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserOrganizationRoleRepository userOrganizationRoleRepository;

    @Mock
    private JpaRoleRepository roleRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    private AuthContextService service;

    private User testUser;
    private Organization testOrg;
    private Store store1;
    private Store store2;
    private Role orgAdminRole;
    private Role cashierRole;

    @BeforeEach
    void setUp() {
        service = new AuthContextService(
                jwtService,
                refreshTokenRepository,
                userOrganizationRoleRepository,
                roleRepository,
                organizationRepository
        );

        testUser = TestDataFactory.createUser("test@example.com");
        testOrg = TestDataFactory.createOrganization("Test Org");
        store1 = TestDataFactory.createStore("Store 1", testOrg);
        store2 = TestDataFactory.createStore("Store 2", testOrg);

        orgAdminRole = TestDataFactory.createRoleWithPermissions(
                "ORG_ADMIN",
                "ORG_READ", "ORG_WRITE", "STORE_READ", "STORE_WRITE", "TEMPLATE_READ"
        );
        cashierRole = TestDataFactory.createRoleWithPermissions(
                "CASHIER",
                "SALE_READ", "SALE_WRITE"
        );
    }

    @Nested
    @DisplayName("buildContextForOrganization")
    class BuildContextForOrganization {

        @Test
        @DisplayName("Should build context with correct organization and store")
        void buildsContextWithOrgAndStore() {
            // Given
            UserOrganizationRole orgRole = TestDataFactory.createOrgLevelRole(testUser, testOrg, orgAdminRole);
            List<UserOrganizationRole> roles = List.of(orgRole);

            when(userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(testUser.getId()))
                    .thenReturn(roles);
            when(roleRepository.findByIdsWithPermissions(any()))
                    .thenReturn(List.of(orgAdminRole));

            // When
            ActiveContext context = service.buildContextForOrganization(testUser, testOrg, store1);

            // Then
            assertThat(context).isNotNull();
            assertThat(context.organizationId()).isEqualTo(testOrg.getId());
            assertThat(context.storeId()).isEqualTo(store1.getId());
            assertThat(context.organization()).isEqualTo(testOrg);
            assertThat(context.store()).isEqualTo(store1);
        }

        @Test
        @DisplayName("Should build context with null store ID when no store specified")
        void buildsContextWithNullStoreId() {
            // Given
            UserOrganizationRole orgRole = TestDataFactory.createOrgLevelRole(testUser, testOrg, orgAdminRole);
            List<UserOrganizationRole> roles = List.of(orgRole);

            when(userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(testUser.getId()))
                    .thenReturn(roles);
            when(roleRepository.findByIdsWithPermissions(any()))
                    .thenReturn(List.of(orgAdminRole));

            // When
            ActiveContext context = service.buildContextForOrganization(testUser, testOrg, null);

            // Then
            assertThat(context.storeId()).isNull();
            assertThat(context.store()).isNull();
        }

        @Test
        @DisplayName("Should include authorities from role permissions")
        void includesAuthoritiesFromPermissions() {
            // Given
            UserOrganizationRole orgRole = TestDataFactory.createOrgLevelRole(testUser, testOrg, orgAdminRole);
            List<UserOrganizationRole> roles = List.of(orgRole);

            when(userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(testUser.getId()))
                    .thenReturn(roles);
            when(roleRepository.findByIdsWithPermissions(any()))
                    .thenReturn(List.of(orgAdminRole));

            // When
            ActiveContext context = service.buildContextForOrganization(testUser, testOrg, null);

            // Then
            List<String> authorityNames = context.authorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            assertThat(authorityNames).contains("ROLE_ORG_ADMIN");
            assertThat(authorityNames).contains("ORG_READ", "ORG_WRITE", "STORE_READ", "TEMPLATE_READ");
        }
    }

    @Nested
    @DisplayName("buildAuthUserDTO - Store Visibility")
    class BuildAuthUserDTOStoreVisibility {

        @Test
        @DisplayName("Should return all stores for user with org-level role")
        void returnsAllStoresForOrgLevelRole() {
            // Given
            UserOrganizationRole orgRole = TestDataFactory.createOrgLevelRole(testUser, testOrg, orgAdminRole);
            List<UserOrganizationRole> roles = List.of(orgRole);

            when(userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(testUser.getId()))
                    .thenReturn(roles);
            when(roleRepository.findByIdsWithPermissions(any()))
                    .thenReturn(List.of(orgAdminRole));
            when(organizationRepository.findByIdWithStores(testOrg.getId()))
                    .thenReturn(Optional.of(testOrg));

            ActiveContext context = service.buildContextForOrganization(testUser, testOrg, null);

            // When
            AuthUserDTO dto = service.buildAuthUserDTO(testUser, context);

            // Then
            assertThat(dto.getActiveOrganization()).isNotNull();
            assertThat(dto.getActiveOrganization().getStores())
                    .hasSize(2)
                    .extracting(StoreDTO::getName)
                    .containsExactlyInAnyOrder("Store 1", "Store 2");
        }

        @Test
        @DisplayName("Should return only assigned stores for user with store-level role")
        void returnsOnlyAssignedStoresForStoreLevelRole() {
            // Given
            UserOrganizationRole storeRole = TestDataFactory.createStoreLevelRole(
                    testUser, testOrg, store1, cashierRole
            );
            List<UserOrganizationRole> roles = List.of(storeRole);

            when(userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(testUser.getId()))
                    .thenReturn(roles);
            when(roleRepository.findByIdsWithPermissions(any()))
                    .thenReturn(List.of(cashierRole));

            ActiveContext context = service.buildContextForOrganization(testUser, testOrg, store1);

            // When
            AuthUserDTO dto = service.buildAuthUserDTO(testUser, context);

            // Then
            assertThat(dto.getActiveOrganization()).isNotNull();
            assertThat(dto.getActiveOrganization().getStores())
                    .hasSize(1)
                    .extracting(StoreDTO::getName)
                    .containsExactly("Store 1");
        }

        @Test
        @DisplayName("Should return all stores when user has mixed org and store level roles")
        void returnsAllStoresForMixedRoles() {
            // Given
            UserOrganizationRole orgRole = TestDataFactory.createOrgLevelRole(testUser, testOrg, orgAdminRole);
            UserOrganizationRole storeRole = TestDataFactory.createStoreLevelRole(
                    testUser, testOrg, store1, cashierRole
            );
            List<UserOrganizationRole> roles = List.of(orgRole, storeRole);

            when(userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(testUser.getId()))
                    .thenReturn(roles);
            when(roleRepository.findByIdsWithPermissions(any()))
                    .thenReturn(List.of(orgAdminRole, cashierRole));
            when(organizationRepository.findByIdWithStores(testOrg.getId()))
                    .thenReturn(Optional.of(testOrg));

            ActiveContext context = service.buildContextForOrganization(testUser, testOrg, null);

            // When
            AuthUserDTO dto = service.buildAuthUserDTO(testUser, context);

            // Then
            assertThat(dto.getActiveOrganization()).isNotNull();
            assertThat(dto.getActiveOrganization().getStores())
                    .hasSize(2); // Should see all stores due to org-level role
        }
    }

    @Nested
    @DisplayName("buildAuthUserDTO - General")
    class BuildAuthUserDTOGeneral {

        @Test
        @DisplayName("Should include user details in DTO")
        void includesUserDetails() {
            // Given
            UserOrganizationRole orgRole = TestDataFactory.createOrgLevelRole(testUser, testOrg, orgAdminRole);
            List<UserOrganizationRole> roles = List.of(orgRole);

            when(userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(testUser.getId()))
                    .thenReturn(roles);
            when(roleRepository.findByIdsWithPermissions(any()))
                    .thenReturn(List.of(orgAdminRole));
            when(organizationRepository.findByIdWithStores(testOrg.getId()))
                    .thenReturn(Optional.of(testOrg));

            ActiveContext context = service.buildContextForOrganization(testUser, testOrg, null);

            // When
            AuthUserDTO dto = service.buildAuthUserDTO(testUser, context);

            // Then
            assertThat(dto.getId()).isEqualTo(testUser.getId());
            assertThat(dto.getEmail()).isEqualTo(testUser.getEmail());
            assertThat(dto.getFirstName()).isEqualTo(testUser.getFirstName());
            assertThat(dto.getLastName()).isEqualTo(testUser.getLastName());
            assertThat(dto.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should include roles in DTO")
        void includesRoles() {
            // Given
            UserOrganizationRole orgRole = TestDataFactory.createOrgLevelRole(testUser, testOrg, orgAdminRole);
            List<UserOrganizationRole> roles = List.of(orgRole);

            when(userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(testUser.getId()))
                    .thenReturn(roles);
            when(roleRepository.findByIdsWithPermissions(any()))
                    .thenReturn(List.of(orgAdminRole));
            when(organizationRepository.findByIdWithStores(testOrg.getId()))
                    .thenReturn(Optional.of(testOrg));

            ActiveContext context = service.buildContextForOrganization(testUser, testOrg, null);

            // When
            AuthUserDTO dto = service.buildAuthUserDTO(testUser, context);

            // Then
            assertThat(dto.getRoles()).contains("ORG_ADMIN");
        }

        @Test
        @DisplayName("Should include permissions in DTO")
        void includesPermissions() {
            // Given
            UserOrganizationRole orgRole = TestDataFactory.createOrgLevelRole(testUser, testOrg, orgAdminRole);
            List<UserOrganizationRole> roles = List.of(orgRole);

            when(userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(testUser.getId()))
                    .thenReturn(roles);
            when(roleRepository.findByIdsWithPermissions(any()))
                    .thenReturn(List.of(orgAdminRole));
            when(organizationRepository.findByIdWithStores(testOrg.getId()))
                    .thenReturn(Optional.of(testOrg));

            ActiveContext context = service.buildContextForOrganization(testUser, testOrg, null);

            // When
            AuthUserDTO dto = service.buildAuthUserDTO(testUser, context);

            // Then
            assertThat(dto.getPermissions())
                    .contains("ORG_READ", "ORG_WRITE", "STORE_READ", "TEMPLATE_READ");
        }
    }
}
