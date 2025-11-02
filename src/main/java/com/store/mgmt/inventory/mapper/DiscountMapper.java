package com.store.mgmt.inventory.mapper;
import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.inventory.model.dto.CreateDiscountDTO;
import com.store.mgmt.inventory.model.dto.DiscountDTO;
import com.store.mgmt.inventory.model.dto.UpdateDiscountDTO;
import com.store.mgmt.inventory.model.entity.Discount;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", 
        config = BaseMapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DiscountMapper {
    @Mapping(target = "percentage", ignore = true) // Not in entity or calculated field
    DiscountDTO toDto(Discount discount);
    List<DiscountDTO> toDtoList(List<Discount> discounts);
    
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
    @Mapping(target = "value", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "productTemplate", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "minimumPurchaseAmount", ignore = true)
    @Mapping(target = "minimumItemQuantity", ignore = true)
    Discount toEntity(CreateDiscountDTO createDTO);
    
    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "active", ignore = true)
    void updateDiscountFromDto(UpdateDiscountDTO updateDTO, @MappingTarget Discount discount);
}