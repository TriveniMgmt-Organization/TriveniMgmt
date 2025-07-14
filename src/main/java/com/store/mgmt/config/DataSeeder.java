package com.store.mgmt.config;

import com.store.mgmt.users.model.entity.Permission;
import com.store.mgmt.users.model.entity.Role;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.PermissionRepository;
import com.store.mgmt.users.repository.RoleRepository;
import com.store.mgmt.users.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

@Component
public class DataSeeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataSeeder(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void seedData() {
        seedRoles();
        seedUsers();
    }

    private void seedRoles() {
        String[] roles = {"ADMIN", "MANAGER", "USER"};
        for (String roleName : roles) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            }
        }
    }

    private void seedUsers() {
        String[][] users = {
                {"admin", "admin@store.com", "admin123", "ADMIN"},
                {"manager", "manager@store.com", "manager123", "MANAGER"}
        };

        for (String[] userData : users) {
            if (userRepository.findByUsername(userData[1]).isEmpty()) {
                User user = new User();
                user.setFullName(userData[0]);
                user.setUsername(userData[1]);
                user.setEmail(userData[1]);
                user.setPasswordHash(passwordEncoder.encode(userData[2]));
                user.setActive(true);
                user.setCreatedBy("system");
                Role role = roleRepository.findByName(userData[3])
                        .orElseThrow(() -> new RuntimeException("Role not found: " + userData[3]));
                user.setRoles(new HashSet<>(Collections.singletonList(role)));
                userRepository.save(user);
            }
        }
    }
}