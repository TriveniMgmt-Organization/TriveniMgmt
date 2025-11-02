package com.store.mgmt.inventory.mapper;
import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.inventory.model.dto.CreateSaleItemDTO;
import com.store.mgmt.inventory.model.dto.SaleItemDTO;
import com.store.mgmt.inventory.model.dto.UpdateSaleItemDTO;
import com.store.mgmt.inventory.model.entity.SaleItem;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", 
        config = BaseMapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SaleItemMapper {
    @Mapping(source = "sale.id", target = "saleId", qualifiedByName = "uuidToString")
    @Mapping(target = "inventoryItemId", ignore = true) // Complex mapping - handled in service layer
    @Mapping(source = "unitPrice", target = "price")
    @Mapping(source = "productTemplate.id", target = "productTemplateId")
    SaleItemDTO toDto(SaleItem saleItem);
    
    @org.mapstruct.Named("uuidToString")
    default String uuidToString(java.util.UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }
    List<SaleItemDTO> toDtoList(List<SaleItem> saleItems);
    
    // Base method with all BaseEntity ignores - other methods inherit from this
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "sale", ignore = true)
    @Mapping(target = "productTemplate", ignore = true)
    @Mapping(target = "variant", ignore = true)
    SaleItem toEntity(CreateSaleItemDTO createDTO);
    
    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "sale", ignore = true)
    @Mapping(target = "productTemplate", ignore = true)
    @Mapping(target = "variant", ignore = true)
    void updateSaleItemFromDto(UpdateSaleItemDTO updateDTO, @MappingTarget SaleItem saleItem);
}