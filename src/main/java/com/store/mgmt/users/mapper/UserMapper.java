package com.store.mgmt.users.mapper;

import com.store.mgmt.users.model.dto.PermissionDTO;
import com.store.mgmt.users.model.dto.UserDTO;
import com.store.mgmt.users.model.entity.Permission;
import com.store.mgmt.users.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping; // If you need complex mappings
import org.mapstruct.factory.Mappers;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring") // Tells MapStruct to make this a Spring component
public interface UserMapper {
    // Basic mapping from User entity to UserDTO
    // You might need more complex mappings if DTOs/Entities differ significantly
        UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

        @Mapping(target = "permissions", expression = "java(mapPermissions(user))")
        UserDTO toDto(User user);

        User toEntity(UserDTO userDTO);

        // Custom method to extract permissions from roles
        default Set<PermissionDTO> mapPermissions(User user) {
            return user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(this::permissionToPermissionDTO)
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