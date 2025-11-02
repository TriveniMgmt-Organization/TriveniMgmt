package com.store.mgmt.inventory.mapper;
import com.store.mgmt.inventory.model.dto.CreatePurchaseOrderDTO;
import com.store.mgmt.inventory.model.dto.PurchaseOrderItemDTO;
import com.store.mgmt.inventory.model.entity.PurchaseOrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PurchaseOrderItemMapper {
    @Mapping(source = "purchaseOrder.id", target = "purchaseOrderId")
    @Mapping(source = "productTemplate.id", target = "productTemplateId")
    @Mapping(source = "orderedQuantity", target = "quantity")
    @Mapping(source = "unitCost", target = "price")
    @Mapping(source = "unitCost", target = "costPrice")
    @Mapping(source = "variant.retailPrice", target = "retailPrice")
    @Mapping(target = "batchNumber", ignore = true)
    @Mapping(target = "lotNumber", ignore = true)
    @Mapping(target = "expirationDate", ignore = true)
    @Mapping(target = "locationId", ignore = true)
    PurchaseOrderItemDTO toDto(PurchaseOrderItem purchaseOrderItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "purchaseOrder", ignore = true)
    @Mapping(target = "productTemplate", ignore = true)
    @Mapping(target = "orderedQuantity", ignore = true)
    @Mapping(target = "receivedQuantity", ignore = true)
    @Mapping(target = "unitCost", ignore = true)
    @Mapping(target = "variant", ignore = true)
    PurchaseOrderItem toEntity(CreatePurchaseOrderDTO purchaseOrderItemDTO);
}