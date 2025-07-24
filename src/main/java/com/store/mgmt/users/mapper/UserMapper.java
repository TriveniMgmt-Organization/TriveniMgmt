package com.store.mgmt.users.mapper;

import com.store.mgmt.users.model.PermissionType;
import com.store.mgmt.users.model.RoleType;
import com.store.mgmt.users.model.dto.PermissionDTO;
import com.store.mgmt.users.model.dto.UpdateUserDTO;
import com.store.mgmt.users.model.dto.UserDTO;
import com.store.mgmt.users.model.entity.Permission;
import com.store.mgmt.users.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping; // If you need complex mappings
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "permissions", expression = "java(mapPermissions(user))")
    @Mapping(target = "roles", expression = "java(mapRoles(user))")
    UserDTO toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserDTO userDTO);
    @Mapping(target = "id", ignore = true) // Don't update the ID
    @Mapping(target = "createdAt", ignore = true) // Don't update creation timestamp
    @Mapping(target = "updatedAt", ignore = true) // Let @UpdateTimestamp handle this

        // @Mapping(target = "username", source = "dto.username")
    void updateEntityFromDto(UpdateUserDTO dto, @MappingTarget User user);
    // Custom method to extract permissions from roles
    default Set<RoleType> mapRoles(User user) {
        return user.getOrganizationRoles().stream()
                  .map(role -> RoleType.valueOf(role.getRole().getName()))
                .collect(Collectors.toSet());
    }

    default Set<PermissionType> mapPermissions(User user) {
        return user.getOrganizationRoles().stream()
                .flatMap(role -> role.getRole().getPermissions().stream())
                .map(per -> PermissionType.valueOf(permissionToPermissionDTO(per).getName()))
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