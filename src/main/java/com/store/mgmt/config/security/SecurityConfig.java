package com.store.mgmt.config.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.store.mgmt.shared.infrastructure.security.JWTService;
import com.store.mgmt.modules.users.domain.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
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
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.Properties;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**"
    };

    private final CustomUserDetailsService userService;
    private final JWTService jwtService;
    private final UserRepository userRepository;

    // JWT Configuration
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    // CORS Configuration
    @Value("${FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    // Mail Configuration - loaded from environment variables
    @Value("${MAIL_HOST:smtp.gmail.com}")
    private String mailHost;

    @Value("${MAIL_PORT:587}")
    private int mailPort;

    @Value("${MAIL_USERNAME:}")
    private String mailUsername;

    @Value("${MAIL_PASSWORD:}")
    private String mailPassword;

    private SecretKey signingKey;

    public SecurityConfig(CustomUserDetailsService userService, JWTService jwtService,
                          UserRepository userRepository) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        validateConfiguration();
        this.signingKey = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        logger.info("SecurityConfig initialized successfully");
    }

    private void validateConfiguration() {
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            throw new IllegalStateException("JWT secret is not configured. Set JWT_SECRET environment variable.");
        }
        if (jwtSecret.length() < 32) {
            logger.warn("JWT secret is shorter than recommended 32 characters");
        }
        logger.debug("Configuration validated: frontendUrl={}", frontendUrl);
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
                                .requestMatchers("/api/v2/auth/login", "/api/v2/auth/register", "/api/v2/auth/refresh").permitAll()
                                .requestMatchers("/api/v2/global-templates/active").permitAll()
                                .requestMatchers(SWAGGER_WHITELIST).permitAll()
                                .requestMatchers("/error").permitAll()
                                .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()

                                // V2 API endpoints - permission based
                                .requestMatchers("/api/v2/global-templates/**").hasAnyAuthority("ROLE_SUPER_ADMIN")
                                .requestMatchers("/api/v2/organizations/**").authenticated()
                                .requestMatchers("/api/v2/stores/**").authenticated()
                                .requestMatchers("/api/v2/users/**").authenticated()
                                .requestMatchers("/api/v2/roles/**").authenticated()
                                .requestMatchers("/api/v2/inventory/**").authenticated()

                                // V1 API endpoints (legacy)
                                .requestMatchers("/api/v1/global-templates/**").hasAnyAuthority("ROLE_SUPER_ADMIN")
                                .requestMatchers("/api/v1/organizations/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_ORG_ADMIN")
                                .requestMatchers("/api/v1/stores/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_ORG_ADMIN", "ROLE_STORE_MANAGER")
                                .requestMatchers("/api/v1/user-organizations/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_ORG_ADMIN")
                                .requestMatchers("/api/v1/admin/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_ADMIN")
                                .requestMatchers("/api/v1/users/**").hasAnyAuthority("USER_READ", "USER_WRITE")
                                .requestMatchers("/api/v1/roles/**").hasAnyAuthority("ROLE_READ", "ROLE_WRITE")
                                .requestMatchers("/api/v1/inventory/products/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "PRODUCT_READ", "PRODUCT_WRITE", "INVENTORY_ITEM_READ", "INVENTORY_ITEM_WRITE")
                                .requestMatchers("/api/v1/inventory/**").hasAnyAuthority("INVENTORY_ITEM_READ", "INVENTORY_ITEM_WRITE")

                                .anyRequest().authenticated()
                )
                .addFilterBefore(new JWTCookieAuthenticationFilter(jwtService, userRepository), UsernamePasswordAuthenticationFilter.class)
                .userDetailsService(userService)
                .headers(headers -> headers
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self';"))
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            logger.warn("Unauthorized access attempt to {}: {}",
                                    request.getRequestURI(), authException.getMessage());
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            logger.warn("Access denied to {} for user: {}",
                                    request.getRequestURI(), accessDeniedException.getMessage());
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                        })
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        if (frontendUrl == null || frontendUrl.trim().isEmpty()) {
            throw new IllegalStateException("FRONTEND_URL is not configured. Set FRONTEND_URL environment variable.");
        }

        logger.info("Configuring CORS with allowed origin: {}", frontendUrl);

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "Accept",
                "X-Requested-With", "X-Store-Id", "X-Organization-Id", "X-Correlation-ID"
        ));
        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "X-Correlation-ID"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(signingKey).build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtIssuer));
        logger.debug("JWT Decoder configured with issuer: {}", jwtIssuer);
        return jwtDecoder;
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        JWKSource<SecurityContext> jwkSource = new ImmutableSecret<>(signingKey);
        logger.debug("JWT Encoder configured");
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public SecretKey jwtSigningKey() {
        return signingKey;
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
    @PostConstruct
    public void configureSecurityContext() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);

        // Only configure authentication if credentials are provided
        if (mailUsername != null && !mailUsername.isEmpty()) {
            mailSender.setUsername(mailUsername);
            mailSender.setPassword(mailPassword);

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.debug", "false"); // Disable debug in production

            logger.info("Mail sender configured with host: {}, port: {}", mailHost, mailPort);
        } else {
            logger.warn("Mail credentials not configured. Email functionality will be disabled.");
        }

        return mailSender;
    }
}
