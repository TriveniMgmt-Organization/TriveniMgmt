package com.store.mgmt.inventory.mapper;
import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.inventory.model.dto.CreateDamageLossDTO;
import com.store.mgmt.inventory.model.dto.DamageLossDTO;
import com.store.mgmt.inventory.model.dto.UpdateDamageLossDTO;
import com.store.mgmt.inventory.model.entity.DamageLoss;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        config = BaseMapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DamageLossMapper {
    @Mapping(target = "inventoryItemId", ignore = true) // Complex mapping - handled in service
    @Mapping(target = "description", ignore = true) // Not in entity or handled separately
    @Mapping(target = "date", ignore = true) // Not in entity or mapped differently
    DamageLossDTO toDto(DamageLoss damageLoss);
    List<DamageLossDTO> toDtoList(List<DamageLoss> damageLosses);
    
    // Base method with all BaseEntity ignores - other methods inherit from this
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "productTemplate", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "dateRecorded", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "variant", ignore = true)
    DamageLoss toEntity(CreateDamageLossDTO createDTO);
    
    @InheritConfiguration(name = "toEntity")
    void updateDamageLossFromDto(UpdateDamageLossDTO updateDTO, @MappingTarget DamageLoss damageLoss);
}