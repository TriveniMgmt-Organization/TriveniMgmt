package com.store.mgmt.config.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.store.mgmt.auth.service.JWTService;
import com.store.mgmt.organization.repository.OrganizationRepository;
import com.store.mgmt.organization.repository.StoreRepository;
import com.store.mgmt.users.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.util.RateLimiter;
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
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
//@Profile({"dev", "test", "prod"}) // Adjust profiles as needed
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final CustomUserDetailsService userService;
    private final JWTService jwtService;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final StoreRepository storeRepository;
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
    public SecurityConfig(CustomUserDetailsService userService, JWTService jwtService,
                          UserRepository userRepository, OrganizationRepository organizationRepository,
                          StoreRepository storeRepository
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.storeRepository = storeRepository;
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
                                .requestMatchers("/error").permitAll()
                                .requestMatchers("/actuator/health").permitAll()
                                .requestMatchers("/api/v1/global-templates/**").hasAnyAuthority("ROLE_SUPER_ADMIN")
                                .requestMatchers("/api/v1/organizations/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_ORG_ADMIN")
                                .requestMatchers("/api/v1/stores/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_ORG_ADMIN", "ROLE_STORE_MANAGER")
                                .requestMatchers("/api/v1/user-organizations/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_ORG_ADMIN")
                                // Role-based endpoint restrictions
                                .requestMatchers("/api/v1/admin/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_ADMIN")
//                        .requestMatchers("/api/v1/manager/**").hasAnyAuthority("ROLE_MANAGER")
//                        .requestMatchers("/api/v1/cashier/**").hasAnyAuthority("ROLE_CASHIER")
//                        .requestMatchers("/api/v1/support/**").hasAnyAuthority("ROLE_SUPPORT")
//                        .requestMatchers("/api/v1/customer/**").hasAnyAuthority("ROLE_CUSTOMER")
                                // Permission-based restrictions (optional, for finer control)
                                .requestMatchers("/api/v1/users/**").hasAnyAuthority("USER_READ", "USER_WRITE")
                                .requestMatchers("/api/v1/roles/**").hasAnyAuthority("ROLE_READ", "ROLE_WRITE")
                                // Product endpoints (templates) require PRODUCT_READ/PRODUCT_WRITE or ROLE_SUPER_ADMIN
                                .requestMatchers("/api/v1/inventory/products/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "PRODUCT_READ", "PRODUCT_WRITE", "INVENTORY_ITEM_READ", "INVENTORY_ITEM_WRITE")
                                // Other inventory endpoints require INVENTORY_ITEM_READ/INVENTORY_ITEM_WRITE
                                .requestMatchers("/api/v1/inventory/**").hasAnyAuthority("INVENTORY_ITEM_READ", "INVENTORY_ITEM_WRITE")
//                        .requestMatchers("/api/v1/inventory/**").permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(new JWTCookieAuthenticationFilter(jwtService, userRepository ), UsernamePasswordAuthenticationFilter.class)
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
                            if (accessDeniedException.getMessage().contains("No resources available")) {
                                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No resources available");
                            } else {
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                            }
                            accessDeniedException.printStackTrace();
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
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With", "X-Store-Id", "X-Organization-Id"));
//        configuration.setAllowedHeaders(Arrays.asList("*"));
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
    @PostConstruct
    public void configureSecurityContext() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com"); // Replace with your SMTP host
        mailSender.setPort(587); // Replace with your SMTP port
        mailSender.setUsername("prasubd@gmail.com"); // Replace with your email
        mailSender.setPassword("prasubd@123"); // Replace with your email password

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

//    @Bean
//    public RateLimiter rateLimiter() {
//        return RateLimiter.of("auth", RateLimiterConfig.custom()
//                .limitForPeriod(10)
//                .timeoutDuration(Duration.ofSeconds(1))
//                .build());
//    }
}
