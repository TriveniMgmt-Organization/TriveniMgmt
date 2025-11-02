package com.store.mgmt.users.mapper;

import com.store.mgmt.users.model.PermissionType;
import com.store.mgmt.users.model.RoleType;
import com.store.mgmt.users.model.dto.PermissionDTO;
import com.store.mgmt.users.model.dto.UpdateUserDTO;
import com.store.mgmt.users.model.dto.UserDTO;
import com.store.mgmt.users.model.entity.Permission;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.common.mapper.BaseMapperConfig;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", config = BaseMapperConfig.class)
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "permissions", expression = "java(mapPermissions(user))")
    @Mapping(target = "roles", expression = "java(mapRoles(user))")
    @Mapping(target = "activeOrganization", ignore = true) // Complex mapping - handled separately
    @Mapping(target = "activeStore", ignore = true) // Complex mapping - handled separately
    UserDTO toDto(User user);

    // Base method with all BaseEntity ignores - other methods inherit from this
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "organizationRoles", ignore = true)
    User toEntity(UserDTO userDTO);
    
    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "organizationRoles", ignore = true)
    void updateEntityFromDto(UpdateUserDTO dto, @MappingTarget User user);
    // Custom method to extract permissions from roles
    default Set<RoleType> mapRoles(User user) {
        if (user == null || user.getOrganizationRoles() == null) {
            return java.util.Collections.emptySet();
        }
        return user.getOrganizationRoles().stream()
                  .filter(role -> role != null && role.getRole() != null && role.getRole().getName() != null)
                  .map(role -> {
                      String roleName = role.getRole().getName();
                      if (roleName == null || roleName.trim().isEmpty()) {
                          return null;
                      }
                      try {
                          return RoleType.valueOf(roleName.toUpperCase().trim());
                      } catch (IllegalArgumentException e) {
                          return null;
                      }
                  })
                  .filter(java.util.Objects::nonNull)
                  .collect(Collectors.toSet());
    }

    default Set<PermissionType> mapPermissions(User user) {
        if (user == null || user.getOrganizationRoles() == null) {
            return java.util.Collections.emptySet();
        }
        return user.getOrganizationRoles().stream()
                .filter(role -> role != null && role.getRole() != null && role.getRole().getPermissions() != null)
                .flatMap(role -> role.getRole().getPermissions().stream())
                .filter(per -> per != null)
                .map(per -> {
                    PermissionDTO permDTO = permissionToPermissionDTO(per);
                    if (permDTO == null || permDTO.getName() == null || permDTO.getName().trim().isEmpty()) {
                        return null;
                    }
                    try {
                        return PermissionType.valueOf(permDTO.getName().toUpperCase().trim());
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
    }

    // Add a helper method to map Permission to PermissionDTO
    default PermissionDTO permissionToPermissionDTO(Permission permission) {
        if (permission == null) {
            return null;
        }
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setId(permission.getId());
        permissionDTO.setName(permission.getName());
        permissionDTO.setDescription(permission.getDescription());
        return permissionDTO;
    }
}