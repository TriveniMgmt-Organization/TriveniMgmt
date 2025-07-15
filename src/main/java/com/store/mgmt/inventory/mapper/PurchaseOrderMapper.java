package com.store.mgmt.inventory.mapper;
import com.store.mgmt.inventory.model.dto.CreatePurchaseOrderDTO;
import com.store.mgmt.inventory.model.dto.PurchaseOrderDTO;
import com.store.mgmt.inventory.model.dto.UpdatePurchaseOrderDTO;
import com.store.mgmt.inventory.model.entity.PurchaseOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PurchaseOrderMapper {
     PurchaseOrderDTO toDto(PurchaseOrder purchaseOrder);
     List<PurchaseOrderDTO> toDtoList(List<PurchaseOrder> purchaseOrders);
     @Mapping(target = "id", ignore = true) // Assuming id is auto-generated and should not be set from DTO
     PurchaseOrder toEntity(CreatePurchaseOrderDTO purchaseOrderDto);

     @Mapping(target = "id", ignore = true)
        void updatePurchaseOrderFromDto(UpdatePurchaseOrderDTO purchaseOrderDto, @MappingTarget PurchaseOrder purchaseOrder);
}