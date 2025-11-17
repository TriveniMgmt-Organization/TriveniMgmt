package com.store.mgmt.inventory.mapper;
import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.inventory.model.dto.CreateInventoryItemDTO;
import com.store.mgmt.inventory.model.dto.InventoryItemDTO;
import com.store.mgmt.inventory.model.dto.StockLevelDTO;
import com.store.mgmt.inventory.model.entity.InventoryItem;
import com.store.mgmt.inventory.model.entity.StockLevel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        config = BaseMapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {StockLevelMapper.class}) // Use StockLevelMapper for nested mapping
public interface InventoryItemMapper {
    @Mapping(source = "variant.id", target = "variantId")
    @Mapping(source = "location.id", target = "locationId")
    @Mapping(source = "batchLot.id", target = "batchLotId")
    @Mapping(target = "stockLevel", ignore = true) // Ignore stockLevel to prevent circular reference
    InventoryItemDTO toDto(InventoryItem entity);

    List<InventoryItemDTO> toDtoList(List<InventoryItem> entities);

    // Base method with all BaseEntity ignores
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "variant", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "batchLot", ignore = true)
    @Mapping(target = "stockLevel", ignore = true)
    InventoryItem toEntity(CreateInventoryItemDTO dto);
}