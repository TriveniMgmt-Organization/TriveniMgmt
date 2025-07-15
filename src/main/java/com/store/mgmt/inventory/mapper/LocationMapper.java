package com.store.mgmt.inventory.mapper;
import com.store.mgmt.inventory.model.dto.CreateLocationDTO;
import com.store.mgmt.inventory.model.dto.LocationDTO;
import com.store.mgmt.inventory.model.entity.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LocationMapper {
    // Define mapping methods here if needed
    // For example:
     LocationDTO toDto(Location location);
     List<LocationDTO> toDtoList(List<Location> locations);
     @Mapping(target = "id", ignore = true)
     Location toEntity(CreateLocationDTO locationDto);
    @Mapping(target = "id", ignore = true)
        void updateLocationFromDto(CreateLocationDTO locationDto, @MappingTarget Location location);
}