package com.store.mgmt.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    // You should define these constants where your actual cookie names are,
    // or import them if they're in a separate utility class.
    // For this example, let's assume they are constants.
    // Replace with your actual constant values or actual cookie names.
    private final String ACCESS_TOKEN_COOKIE_NAME = "session_token";
    private final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    @Bean
    public OpenAPI customOpenAPI() {
        // Define names for your security schemes in OpenAPI
        final String accessTokenSecuritySchemeName = "AccessTokenCookie";
        final String refreshTokenSecuritySchemeName = "RefreshTokenCookie";

        return new OpenAPI()
                .info(new Info()
                        .title("Store Management API")
                        .version("1.0")
                        .description("API for store management system with users, POS, and inventory modules"))
                // Add security requirements for both cookies.
                // This means an endpoint requires *both* for full authentication flow documentation.
                // If an endpoint only needs one, you would apply @SecurityRequirement on the controller/method.
                .addSecurityItem(new SecurityRequirement().addList(accessTokenSecuritySchemeName))
                .addSecurityItem(new SecurityRequirement().addList(refreshTokenSecuritySchemeName))
                .components(new Components()
                        // Define the Access Token Cookie security scheme
                        .addSecuritySchemes(accessTokenSecuritySchemeName,
                                new SecurityScheme()
                                        .name(ACCESS_TOKEN_COOKIE_NAME) // Actual name of the access token cookie
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .description("Authentication via access token cookie. Set after successful login/refresh."))
                        // Define the Refresh Token Cookie security scheme
                        .addSecuritySchemes(refreshTokenSecuritySchemeName,
                                new SecurityScheme()
                                        .name(REFRESH_TOKEN_COOKIE_NAME) // Actual name of the refresh token cookie
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .description("Refresh token for obtaining new access tokens. Set after successful login."))
                );
    }
}