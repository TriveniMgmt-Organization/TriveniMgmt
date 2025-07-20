package com.store.mgmt.organization.mapper;

import com.store.mgmt.organization.model.dto.*;
import com.store.mgmt.organization.model.entity.Store;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring") // Tells MapStruct to make this a Spring component
public interface StoreMapper {
        StoreMapper INSTANCE = Mappers.getMapper(StoreMapper.class);
        StoreDTO toDto(Store entity);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
        Store toEntity(CreateStoreDTO dto);
    @Mapping(target = "id", ignore = true) // Don't update the ID
    @Mapping(target = "createdAt", ignore = true) // Don't update creation timestamp
    @Mapping(target = "updatedAt", ignore = true) // Let @UpdateTimestamp handle this
        // Or if your DTO has a 'username' field and you want to use its value directly:
        // @Mapping(target = "username", source = "dto.username")
    void updateEntityFromDto(UpdateStoreDTO dto, @MappingTarget Store entity);
}