package com.store.mgmt.inventory.mapper;
import com.store.mgmt.inventory.model.dto.CreateSaleDTO;
import com.store.mgmt.inventory.model.dto.SaleDTO;
import com.store.mgmt.inventory.model.dto.UpdateSaleDTO;
import com.store.mgmt.inventory.model.entity.Sale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SaleMapper {
    // Define mapping methods for Sale entity and DTOs here
    // For example:
     SaleDTO toDto(Sale sale);
     List<SaleDTO> toDtoList(List<Sale> sales);
     @Mapping(target = "id", ignore = true) // ID is generated by JPA
     Sale toEntity(CreateSaleDTO createDTO);
    @Mapping(target = "id", ignore = true) // ID is generated by JPA
     void updateSaleFromDto(UpdateSaleDTO updateDTO, @MappingTarget Sale sale);
}
