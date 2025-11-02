package com.store.mgmt.inventory.mapper;
import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.inventory.model.dto.CreateSaleDTO;
import com.store.mgmt.inventory.model.dto.SaleDTO;
import com.store.mgmt.inventory.model.dto.UpdateSaleDTO;
import com.store.mgmt.inventory.model.entity.Sale;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", 
        config = BaseMapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SaleMapper {
    @Mapping(target = "name", ignore = true) // Not in entity
    @Mapping(target = "description", ignore = true) // Not in entity
    @Mapping(target = "discountPercentage", ignore = true) // Not in entity or calculated
    @Mapping(target = "startDate", ignore = true) // Not in entity
    @Mapping(target = "endDate", ignore = true) // Not in entity
    SaleDTO toDto(Sale sale);
    List<SaleDTO> toDtoList(List<Sale> sales);
    
    // Base method with all BaseEntity ignores - other methods inherit from this
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "saleTimestamp", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "totalDiscountAmount", ignore = true)
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "saleItems", ignore = true)
    Sale toEntity(CreateSaleDTO createDTO);
    
    @InheritConfiguration(name = "toEntity")
    void updateSaleFromDto(UpdateSaleDTO updateDTO, @MappingTarget Sale sale);
}
