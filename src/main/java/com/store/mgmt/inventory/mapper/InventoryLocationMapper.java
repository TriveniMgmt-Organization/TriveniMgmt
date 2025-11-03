package com.store.mgmt.inventory.mapper;
import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.inventory.model.dto.CreateInventoryLocationDTO;
import com.store.mgmt.inventory.model.dto.InventoryLocationDTO;
import com.store.mgmt.inventory.model.dto.UpdateInventoryLocationDTO;
import com.store.mgmt.inventory.model.entity.InventoryLocation;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", 
        config = BaseMapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InventoryLocationMapper {
    @Mapping(target = "description", ignore = true) // Not in entity
    @Mapping(target = "contactNumber", ignore = true) // Not in entity
    InventoryLocationDTO toDto(InventoryLocation location);
    List<InventoryLocationDTO> toDtoList(List<InventoryLocation> inventoryLocations);
    
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
    InventoryLocation toEntity(CreateInventoryLocationDTO locationDto);
    
    @InheritConfiguration(name = "toEntity")
    void updateInventoryLocationFromDto(UpdateInventoryLocationDTO locationDto, @MappingTarget InventoryLocation location);
}