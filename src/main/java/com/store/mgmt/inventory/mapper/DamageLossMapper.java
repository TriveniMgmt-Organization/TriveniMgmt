package com.store.mgmt.inventory.mapper;
import com.store.mgmt.inventory.model.dto.CreateDamageLossDTO;
import com.store.mgmt.inventory.model.dto.DamageLossDTO;
import com.store.mgmt.inventory.model.dto.UpdateDamageLossDTO;
import com.store.mgmt.inventory.model.entity.DamageLoss;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DamageLossMapper {
    // Mapstruct will automatically generate the mapping methods for DamageLoss entity
    // to and from DTOs if you define them in the DTO classes.

    // Example method signatures (to be implemented in DTO classes):
     DamageLossDTO toDto(DamageLoss damageLoss);
     List<DamageLossDTO> toDtoList(List<DamageLoss> damageLosses);
    @Mapping(target = "id", ignore = true)
    DamageLoss toEntity(CreateDamageLossDTO createDTO);
     void updateDamageLossFromDto(UpdateDamageLossDTO updateDTO, @MappingTarget DamageLoss damageLoss);
}