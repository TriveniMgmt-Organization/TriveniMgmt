package com.store.mgmt.modules.auth.application;

import com.store.mgmt.modules.auth.application.command.LoginCommand;
import com.store.mgmt.modules.auth.application.command.LoginHandler;
import com.store.mgmt.modules.auth.application.dto.AuthResponseDTO;
import com.store.mgmt.modules.auth.application.dto.AuthUserDTO;
import com.store.mgmt.modules.auth.application.service.AuthContextService;
import com.store.mgmt.modules.auth.application.service.AuthContextService.ActiveContext;
import com.store.mgmt.modules.auth.application.service.AuthContextService.TokenPair;
import com.store.mgmt.modules.auth.infrastructure.service.AuthCookieService;
import com.store.mgmt.modules.users.domain.model.User;
import com.store.mgmt.modules.users.domain.repository.UserRepository;
import com.store.mgmt.testutils.TestDataFactory;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginHandler")
class LoginHandlerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthContextService authContextService;

    @Mock
    private AuthCookieService authCookieService;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private ActiveContext activeContext;

    private LoginHandler handler;

    private User testUser;

    @BeforeEach
    void setUp() {
        handler = new LoginHandler(
                authenticationManager,
                userRepository,
                authContextService,
                authCookieService
        );

        testUser = TestDataFactory.createUser("test@example.com");
    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("Should successfully authenticate valid user")
        void authenticatesValidUser() {
            // Given
            LoginCommand command = new LoginCommand("test@example.com", "password123", false, response);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByUsernameWithAllRelatedData("test@example.com"))
                    .thenReturn(Optional.of(testUser));
            when(authContextService.determineActiveContext(testUser)).thenReturn(activeContext);
            when(authContextService.generateTokens(testUser, activeContext))
                    .thenReturn(new TokenPair("access-token", "refresh-token"));
            when(authContextService.buildAuthUserDTO(testUser, activeContext))
                    .thenReturn(AuthUserDTO.builder()
                            .id(testUser.getId())
                            .email(testUser.getEmail())
                            .build());

            // When
            AuthResponseDTO result = handler.handle(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUser()).isNotNull();
            assertThat(result.getUser().getEmail()).isEqualTo("test@example.com");

            verify(authCookieService).setAuthCookies("access-token", "refresh-token", response);
        }

        @Test
        @DisplayName("Should throw BadCredentialsException for invalid credentials")
        void throwsExceptionForInvalidCredentials() {
            // Given
            LoginCommand command = new LoginCommand("test@example.com", "wrong-password", false, response);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(BadCredentialsException.class);

            verify(authCookieService, never()).setAuthCookies(any(), any(), any());
        }

        @Test
        @DisplayName("Should throw DisabledException for inactive user")
        void throwsExceptionForInactiveUser() {
            // Given
            testUser.setActive(false);
            LoginCommand command = new LoginCommand("test@example.com", "password123", false, response);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByUsernameWithAllRelatedData("test@example.com"))
                    .thenReturn(Optional.of(testUser));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(DisabledException.class)
                    .hasMessageContaining("inactive");

            verify(authContextService, never()).generateTokens(any(), any());
        }

        @Test
        @DisplayName("Should try email lookup if username lookup fails")
        void triesEmailLookupIfUsernameFails() {
            // Given
            LoginCommand command = new LoginCommand("test@example.com", "password123", false, response);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByUsernameWithAllRelatedData("test@example.com"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmailWithRolesAndPermissions("test@example.com"))
                    .thenReturn(Optional.of(testUser));
            when(authContextService.determineActiveContext(testUser)).thenReturn(activeContext);
            when(authContextService.generateTokens(testUser, activeContext))
                    .thenReturn(new TokenPair("access-token", "refresh-token"));
            when(authContextService.buildAuthUserDTO(testUser, activeContext))
                    .thenReturn(AuthUserDTO.builder().id(testUser.getId()).email(testUser.getEmail()).build());

            // When
            AuthResponseDTO result = handler.handle(command);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).findByUsernameWithAllRelatedData("test@example.com");
            verify(userRepository).findByEmailWithRolesAndPermissions("test@example.com");
        }

        @Test
        @DisplayName("Should throw exception when user not found after successful authentication")
        void throwsExceptionWhenUserNotFound() {
            // Given
            LoginCommand command = new LoginCommand("nonexistent@example.com", "password123", false, response);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByUsernameWithAllRelatedData("nonexistent@example.com"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmailWithRolesAndPermissions("nonexistent@example.com"))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }
}
