package com.store.mgmt.inventory.mapper;
import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.inventory.model.dto.CreateLocationDTO;
import com.store.mgmt.inventory.model.dto.LocationDTO;
import com.store.mgmt.inventory.model.dto.UpdateLocationDTO;
import com.store.mgmt.inventory.model.entity.Location;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", 
        config = BaseMapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LocationMapper {
    @Mapping(target = "description", ignore = true) // Not in entity
    @Mapping(target = "contactNumber", ignore = true) // Not in entity
    LocationDTO toDto(Location location);
    List<LocationDTO> toDtoList(List<Location> locations);
    
    // Base method with all BaseEntity ignores - other methods inherit from this
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "inventoryItems", ignore = true)
    @Mapping(target = "damageLosses", ignore = true)
    Location toEntity(CreateLocationDTO locationDto);
    
    @InheritConfiguration(name = "toEntity")
    void updateLocationFromDto(UpdateLocationDTO locationDto, @MappingTarget Location location);
}