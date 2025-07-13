package com.store.mgmt.auth.controller;

import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isActive()) {
            throw new RuntimeException("User account is inactive");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        List<String> authorities = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream()
                        .map(permission -> "PERM_" + permission.getName()))
                .collect(Collectors.toList());

        String secret = "your-256-bit-secret-key-1234567890abcdef";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject(user.getUsername())
                .claim("authorities", authorities)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        return ResponseEntity.ok(token);
    }
}

class LoginRequest {
    private String username;
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}