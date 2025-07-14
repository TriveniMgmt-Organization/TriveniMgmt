package com.store.mgmt.auth.service;

import com.store.mgmt.auth.controller.AuthController;
import com.store.mgmt.auth.exception.AuthenticationException;
import com.store.mgmt.auth.model.dto.AuthCredentials;
import com.store.mgmt.auth.model.dto.AuthResponse;
import com.store.mgmt.auth.model.dto.RegisterCredentials;
import com.store.mgmt.auth.model.entity.RefreshToken;
import com.store.mgmt.common.exception.ResourceNotFoundException;
import com.store.mgmt.users.mapper.UserMapper;
import com.store.mgmt.users.model.dto.UserDTO;
import com.store.mgmt.users.model.entity.Role;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.RefreshTokenRepository;
import com.store.mgmt.users.repository.RoleRepository;
import com.store.mgmt.users.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final UserMapper userMapper;
    private final RefreshTokenRepository refreshTokenRepository; // New repository for refresh tokens

    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder, JWTService jwtService,
                           UserMapper userMapper, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public AuthResponse authenticateUser(AuthCredentials credentials) {
        logger.debug("Authenticating user: {}", credentials.getUsername());

        User user = userRepository.findByUsername(credentials.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!user.isActive()) {
            logger.warn("User account inactive: {}", credentials.getUsername());
            throw new AuthenticationException("User account is inactive", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(credentials.getPassword(), user.getPasswordHash())) {
            logger.warn("Invalid password for user: {}", credentials.getUsername());
            throw new AuthenticationException("Invalid username or password");
        }

        UserDetails userDetails = createUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        storeRefreshToken(user, refreshToken);

        UserDTO userDTO = userMapper.toDto(user);
        return new AuthResponse(accessToken, refreshToken, userDTO);
    }

    @Override
    public AuthResponse registerUser(RegisterCredentials registrationData) {
        if (userRepository.findByUsername(registrationData.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Username '" + registrationData.getEmail() + "' is already taken.");
        }

        if (userRepository.findByEmail(registrationData.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email '" + registrationData.getEmail() + "' is already registered.");
        }

        User newUser = new User();
        newUser.setFullName(registrationData.getFullName());
        newUser.setUsername(registrationData.getEmail());
        newUser.setEmail(registrationData.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(registrationData.getPassword()));
        newUser.setActive(true);

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default 'USER' role not found."));
        newUser.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        User savedUser = userRepository.save(newUser);

        UserDetails userDetails = createUserDetails(savedUser);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Store refresh token
        storeRefreshToken(savedUser, refreshToken);

        UserDTO userDTO = userMapper.toDto(savedUser);
        return new AuthResponse(accessToken, refreshToken, userDTO);
    }

    public AuthResponse refreshToken(String refreshToken) {
        UserDetails userDetails = loadUserByRefreshToken(refreshToken);
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserDTO userDTO = userMapper.toDto(user);
        return new AuthResponse(newAccessToken, refreshToken, userDTO);
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }


    public AuthResponse validateToken(String token) {
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (jwtService.validateToken(token, user)) {
            UserDTO userDTO = userMapper.toDto(user);
            return new AuthResponse(token, null, userDTO);
        } else {
            throw new AuthenticationException("Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }

    }
    private UserDetails createUserDetails(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream()
                        .map(permission -> new SimpleGrantedAuthority("PERM_" + permission.getName())))
                .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPasswordHash(), user.isActive(),
                true, true, true, authorities);
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