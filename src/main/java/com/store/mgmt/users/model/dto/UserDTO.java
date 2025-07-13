package com.store.mgmt.users.model.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    private Set<RoleDTO> roles;
}