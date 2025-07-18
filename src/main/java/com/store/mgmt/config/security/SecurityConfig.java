package com.store.mgmt.config.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
//@Profile({"dev", "test", "prod"}) // Adjust profiles as needed
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final CustomUserDetailsService userService;
    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**"
    };

    @Value("${FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer}")
    private String jwtIssuer;
    public SecurityConfig(CustomUserDetailsService userService
    ) {
        this.userService = userService;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()
                        // Role-based endpoint restrictions
                        .requestMatchers("/api/v1/admin/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_ADMIN")
//                        .requestMatchers("/api/v1/manager/**").hasAnyAuthority("ROLE_MANAGER")
//                        .requestMatchers("/api/v1/cashier/**").hasAnyAuthority("ROLE_CASHIER")
//                        .requestMatchers("/api/v1/support/**").hasAnyAuthority("ROLE_SUPPORT")
//                        .requestMatchers("/api/v1/customer/**").hasAnyAuthority("ROLE_CUSTOMER")
                        // Permission-based restrictions (optional, for finer control)
                        .requestMatchers("/api/v1/users/**").hasAnyAuthority("USER_READ", "USER_WRITE")
                        .requestMatchers("/api/v1/roles/**").hasAnyAuthority("ROLE_READ", "ROLE_WRITE")
                        .requestMatchers("/api/v1/inventory/**").hasAnyAuthority("INVENTORY_ITEM_READ", "INVENTORY_ITEM_WRITE")
//                        .requestMatchers("/api/v1/inventory/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JWTCookieAuthenticationFilter(jwtDecoder(), jwtAuthenticationConverter()), UsernamePasswordAuthenticationFilter.class)
                .userDetailsService(userService)
                .headers(headers -> headers
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self';"))
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            logger.warn("Unauthorized access attempt: {}", authException.getMessage());
                            authException.printStackTrace();
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            logger.warn("Access denied: {}", accessDeniedException.getMessage());
                            accessDeniedException.printStackTrace();
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                        })
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        if (frontendUrl == null || frontendUrl.trim().isEmpty()) {
            logger.error("FRONTEND_URL is not configured properly");
            throw new IllegalStateException("FRONTEND_URL is not configured");
        }

        System.out.println("Configuring CORS with frontend URL: " + frontendUrl);
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Set-Cookie")); // Important for cookies
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            logger.error("JWT secret key is not configured");
            throw new IllegalStateException("JWT secret key is not configured!");
        }
        System.out.println("Decoding.....................");
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(key).build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtIssuer));
        return jwtDecoder;

//        return NimbusJwtDecoder.withSecretKey(key).build();
    }
    @Bean
    public JwtEncoder jwtEncoder() {
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            logger.error("JWT secret key is not configured");
            throw new IllegalStateException("JWT secret key is not configured!");
        }
        System.out.println("Configuring JWT Encoder with secret: " + jwtSecret);
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JWKSource<SecurityContext> jwkSource = new ImmutableSecret<>(key);
        return new NimbusJwtEncoder(jwkSource);
    }

    private SecretKey getSigningKey() {
        return new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            logger.debug("--- SecurityConfig: JWT Claims (after decoding) ---");
            logger.debug("Subject: {}", jwt.getSubject());
            logger.debug("Issuer: {}", jwt.getIssuer());
            logger.debug("Audience: {}", jwt.getAudience());
            logger.debug("Expiration: {}", jwt.getExpiresAt());
            logger.debug("Issued At: {}", jwt.getIssuedAt());
            logger.debug("All Claims: {}", jwt.getClaims());

            Collection<GrantedAuthority> authorities = grantedAuthoritiesConverter.convert(jwt);
            logger.debug("Extracted Authorities: {}", authorities);
            logger.debug("-----------------------------------------------------");
            return authorities;
        });
        return jwtAuthenticationConverter;
    }
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // todo Optional: Audit logging for authentication events
    @Bean
    public ApplicationListener<AbstractAuthenticationEvent> authenticationEventListener() {
        return event -> {
            if (event instanceof AuthenticationSuccessEvent) {
                Authentication auth = ((AuthenticationSuccessEvent) event).getAuthentication();
                logger.info("Successful authentication for user: {}", auth.getName());
            } else if (event instanceof AbstractAuthenticationFailureEvent) {
                AuthenticationException ex = ((AbstractAuthenticationFailureEvent) event).getException();
                logger.warn("Authentication failed: {}", ex.getMessage());
            }
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
