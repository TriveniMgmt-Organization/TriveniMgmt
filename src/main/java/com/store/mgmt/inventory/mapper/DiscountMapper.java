package com.store.mgmt.inventory.mapper;
import com.store.mgmt.inventory.model.entity.Discount;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public class DiscountMapper {
}