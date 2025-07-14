package com.store.mgmt.auth.service;

import com.store.mgmt.auth.model.dto.AuthCredentials;
import com.store.mgmt.auth.model.dto.AuthResponse;
import com.store.mgmt.auth.exception.AuthenticationException;
import com.store.mgmt.auth.model.dto.RegisterCredentials;

public interface AuthService {

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param credentials The authentication request (username and password).
     * @return AuthResponse containing the JWT token and user details.
     * @throws AuthenticationException if authentication fails (invalid credentials, inactive user).
     */
    AuthResponse authenticateUser(AuthCredentials credentials);

    AuthResponse registerUser(RegisterCredentials registrationData);
    AuthResponse refreshToken(String refreshToken);
    AuthResponse validateToken(String token);
    void logout(String refreshToken);
}