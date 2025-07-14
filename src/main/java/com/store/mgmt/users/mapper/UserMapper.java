package com.store.mgmt.users.mapper;

import com.store.mgmt.users.model.dto.UserDTO;
import com.store.mgmt.users.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping; // If you need complex mappings

@Mapper(componentModel = "spring") // Tells MapStruct to make this a Spring component
public interface UserMapper {
    // Basic mapping from User entity to UserDTO
    // You might need more complex mappings if DTOs/Entities differ significantly
    UserDTO toDto(User user);
     User toEntity(UserDTO userDTO); // Optional: for converting DTO back to entity
}