package com.store.mgmt.auth.service;

import com.store.mgmt.auth.model.dto.AuthCredentials;
import com.store.mgmt.auth.model.dto.AuthResponse;
import com.store.mgmt.auth.model.dto.RegisterCredentials;
import com.store.mgmt.auth.model.entity.RefreshToken;
import com.store.mgmt.users.mapper.UserMapper;
import com.store.mgmt.users.model.dto.UserDTO;
import com.store.mgmt.users.model.entity.Role;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.RefreshTokenRepository;
import com.store.mgmt.users.repository.RoleRepository;
import com.store.mgmt.users.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final UserMapper userMapper;
    private final RefreshTokenRepository refreshTokenRepository; // New repository for refresh tokens
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder, JWTService jwtService,
                            AuthenticationManager authenticationManager,
                           UserMapper userMapper, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    @Transactional
    public AuthResponse authenticateUser(AuthCredentials credentials) {
        logger.info("Authenticating user: {}", credentials.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword())
            );

//            manual check remove later
//            if (!passwordEncoder.matches(credentials.getPassword(), user.getPasswordHash())) {
//                logger.warn("Invalid password for user: {}", credentials.getUsername());
//                throw new AuthenticationException("Invalid username or password");
//            }
            User user = userRepository.findByUsername(credentials.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("Invalid username"));

            if (!user.isActive()) {
                logger.warn("User account inactive: {}", credentials.getUsername());
                throw new DisabledException("User account is inactive");
            }

            UserDetails userDetails = createUserDetails(user);
            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            storeRefreshToken(user, refreshToken);

            UserDTO userDTO = userMapper.toDto(user);
            return new AuthResponse(accessToken, refreshToken, userDTO);
        } catch (BadCredentialsException e) {
            logger.warn("Invalid credentials for user: {}", credentials.getUsername());
            throw e;
        } catch (DisabledException e) {
            logger.warn("Account disabled for user: {}", credentials.getUsername());
            throw e;
        } catch (Exception e) {
            logger.error("Authentication failed for user: {}", credentials.getUsername(), e);
            throw new BadCredentialsException("Authentication failed", e);
        }
    }
    @Override
    @Transactional
    public AuthResponse registerUser(RegisterCredentials registrationData) {
        logger.info("Registering user: {}", registrationData.getEmail());

        if (userRepository.findByUsername(registrationData.getEmail()).isPresent()) {
            logger.warn("Username already taken: {}", registrationData.getEmail());
            throw new IllegalArgumentException("Username '" + registrationData.getEmail() + "' is already taken.");
        }

        if (userRepository.findByEmail(registrationData.getEmail()).isPresent()) {
            logger.warn("Email already registered: {}", registrationData.getEmail());
            throw new IllegalArgumentException("Email '" + registrationData.getEmail() + "' is already registered.");
        }

        User newUser = new User();
        newUser.setFirstName(registrationData.getFullName());
        newUser.setUsername(registrationData.getEmail());
        newUser.setEmail(registrationData.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(registrationData.getPassword()));
        newUser.setActive(true);

        Role userRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new IllegalStateException("Default 'CUSTOMER' role not found."));
        newUser.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        User savedUser = userRepository.save(newUser);

        UserDetails userDetails = createUserDetails(savedUser);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        storeRefreshToken(savedUser, refreshToken);

        UserDTO userDTO = userMapper.toDto(savedUser);
        return new AuthResponse(accessToken, refreshToken, userDTO);
    }
    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        logger.info("Refreshing token");

        UserDetails userDetails = loadUserByRefreshToken(refreshToken);
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Invalidate old refresh token
        refreshTokenRepository.deleteByToken(refreshToken);

        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        storeRefreshToken(user, newRefreshToken);

        UserDTO userDTO = userMapper.toDto(user);
        return new AuthResponse(newAccessToken, newRefreshToken, userDTO);
    }
    @Override
    @Transactional
    public void logout(String refreshToken) {
        logger.info("Logging out user");
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    @Override
    public AuthResponse validateToken(String token) {
        logger.info("Validating token for user: {}", jwtService.extractUsername(token));

        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (jwtService.validateToken(token, user)) {
            UserDTO userDTO = userMapper.toDto(user);
            return new AuthResponse(token, null, userDTO);
        } else {
            logger.warn("Invalid or expired token for user: {}", username);
            throw new JwtException("Invalid or expired token");
        }
    }
    private UserDetails createUserDetails(User user) {
        // Include both roles (with ROLE_ prefix) and permissions (without prefix)
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.addAll(user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .toList());
        authorities.addAll(user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                user.isActive(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities
        );
    }

    private void storeRefreshToken(User user, String refreshToken) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(refreshToken);
        token.setExpiryDate(new Date(System.currentTimeMillis() + jwtService.getRefreshTokenExpiration()));
        refreshTokenRepository.save(token);
    }

    private UserDetails loadUserByRefreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new JwtException("Invalid refresh token"));
        if (token.getExpiryDate().before(new Date())) {
            refreshTokenRepository.delete(token);
            throw new JwtException("Refresh token expired");
        }
        User user = token.getUser();
        return createUserDetails(user);
    }
}