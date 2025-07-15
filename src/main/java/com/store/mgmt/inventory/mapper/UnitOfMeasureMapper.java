package com.store.mgmt.inventory.mapper;

import com.store.mgmt.inventory.model.dto.CreateUnitOfMeasureDTO;
import com.store.mgmt.inventory.model.dto.UnitOfMeasureDTO;
import com.store.mgmt.inventory.model.dto.UpdateUnitOfMeasureDTO;
import com.store.mgmt.inventory.model.entity.UnitOfMeasure;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UnitOfMeasureMapper {
    // Define mapping methods here if needed
    // For example, if you have DTOs and entities for UnitOfMeasure, you can add methods like:
     UnitOfMeasureDTO toDto(UnitOfMeasure entity);
     List<UnitOfMeasureDTO> toDtoList(List<UnitOfMeasure> entities);
     @Mapping(target = "id", ignore = true) // Ignore ID for creation, if applicable
     UnitOfMeasure toEntity(CreateUnitOfMeasureDTO dto);
    @Mapping(target = "id", ignore = true) // Ignore ID for creation, if applicable
    void updateUnitOfMeasureFromDto(UpdateUnitOfMeasureDTO dto, @MappingTarget UnitOfMeasure entity);
}