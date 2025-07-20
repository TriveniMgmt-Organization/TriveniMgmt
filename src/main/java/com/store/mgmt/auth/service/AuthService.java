package com.store.mgmt.auth.service;

import com.store.mgmt.auth.model.dto.AuthCredentials;
import com.store.mgmt.auth.model.dto.AuthResponse;
import com.store.mgmt.auth.exception.AuthenticationException;
import com.store.mgmt.auth.model.dto.RegisterCredentials;
import com.store.mgmt.organization.model.dto.OrganizationDTO;
import com.store.mgmt.organization.model.dto.CreateTenantDTO;
import com.store.mgmt.users.model.dto.UserDTO;

import java.util.List;

public interface AuthService {

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param credentials The authentication request (username and password).
     * @return AuthResponse containing the JWT token and user details.
     * @throws AuthenticationException if authentication fails (invalid credentials, inactive user).
     */
    AuthResponse authenticateUser(AuthCredentials credentials);

    /**
     * Retrieves the currently authenticated user.
     *
     * @return UserDTO containing the details of the current user.
     */
    UserDTO getCurrentUser();

    AuthResponse registerUser(RegisterCredentials registrationData);
    AuthResponse refreshToken(String refreshToken);
    UserDTO validateToken(String token);
    void logout(String refreshToken);

    List<OrganizationDTO> getOrganizations();

    AuthResponse selectTenant(CreateTenantDTO selectDTO);
}