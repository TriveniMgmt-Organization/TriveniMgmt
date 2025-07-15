package com.store.mgmt.inventory.mapper;
import com.store.mgmt.inventory.model.dto.CreatePurchaseOrderDTO;
import com.store.mgmt.inventory.model.dto.PurchaseOrderItemDTO;
import com.store.mgmt.inventory.model.entity.PurchaseOrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PurchaseOrderItemMapper {
     PurchaseOrderItemDTO toDto(PurchaseOrderItem purchaseOrderItem);

     PurchaseOrderItem toEntity(CreatePurchaseOrderDTO purchaseOrderItemDTO);
}