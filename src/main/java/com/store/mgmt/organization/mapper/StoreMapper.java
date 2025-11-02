package com.store.mgmt.organization.mapper;

import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.organization.model.dto.*;
import com.store.mgmt.organization.model.entity.Store;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", config = BaseMapperConfig.class)
public interface StoreMapper {
    StoreMapper INSTANCE = Mappers.getMapper(StoreMapper.class);
    
    @Mapping(source = "organization.id", target = "organizationId")
    StoreDTO toDto(Store entity);
    
    // Base method with all BaseEntity ignores - other methods inherit from this
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "countryCode", ignore = true)
    Store toEntity(CreateStoreDTO dto);
    
    @InheritConfiguration(name = "toEntity")
    void updateEntityFromDto(UpdateStoreDTO dto, @MappingTarget Store entity);
}