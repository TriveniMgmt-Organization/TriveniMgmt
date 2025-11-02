package com.store.mgmt.organization.mapper;

import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.organization.model.dto.CreateOrganizationDTO;
import com.store.mgmt.organization.model.dto.OrganizationDTO;
import com.store.mgmt.organization.model.dto.UpdateOrganizationDTO;
import com.store.mgmt.organization.model.entity.Organization;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", config = BaseMapperConfig.class)
public interface OrganizationMapper {
    OrganizationMapper INSTANCE = Mappers.getMapper(OrganizationMapper.class);

    @Mapping(target = "stores", ignore = true) // Ignore organizationId mapping in StoreDTO - it's handled separately
    OrganizationDTO toDto(Organization organization);
    
    // Base method with all BaseEntity ignores - other methods inherit from this
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "stores", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    Organization toEntity(CreateOrganizationDTO dto);
    
    @InheritConfiguration(name = "toEntity")
    void updateEntityFromDto(UpdateOrganizationDTO dto, @MappingTarget Organization entity);
}