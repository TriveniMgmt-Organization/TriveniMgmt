package com.store.mgmt.inventory.mapper;

import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.inventory.model.dto.CreateUnitOfMeasureDTO;
import com.store.mgmt.inventory.model.dto.UnitOfMeasureDTO;
import com.store.mgmt.inventory.model.dto.UpdateUnitOfMeasureDTO;
import com.store.mgmt.inventory.model.entity.UnitOfMeasure;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", 
        config = BaseMapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UnitOfMeasureMapper {
    @Mapping(target = "description", ignore = true) // Not in entity
    @Mapping(target = "conversionFactor", ignore = true) // Not in entity
    UnitOfMeasureDTO toDto(UnitOfMeasure entity);
    List<UnitOfMeasureDTO> toDtoList(List<UnitOfMeasure> entities);
    
    // Base method with all BaseEntity ignores - other methods inherit from this
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "organization", ignore = true)
    UnitOfMeasure toEntity(CreateUnitOfMeasureDTO dto);
    
    @InheritConfiguration(name = "toEntity")
    void updateUnitOfMeasureFromDto(UpdateUnitOfMeasureDTO dto, @MappingTarget UnitOfMeasure entity);
}