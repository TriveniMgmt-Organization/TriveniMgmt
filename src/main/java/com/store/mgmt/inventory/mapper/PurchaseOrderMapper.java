package com.store.mgmt.inventory.mapper;
import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.inventory.model.dto.CreatePurchaseOrderDTO;
import com.store.mgmt.inventory.model.dto.PurchaseOrderDTO;
import com.store.mgmt.inventory.model.dto.UpdatePurchaseOrderDTO;
import com.store.mgmt.inventory.model.entity.PurchaseOrder;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", 
        config = BaseMapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PurchaseOrderMapper {
    @Mapping(source = "supplier.name", target = "supplierName")
    @Mapping(source = "totalEstimatedAmount", target = "totalAmount")
    @Mapping(source = "createdAt", target = "createdDate", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    PurchaseOrderDTO toDto(PurchaseOrder purchaseOrder);
    List<PurchaseOrderDTO> toDtoList(List<PurchaseOrder> purchaseOrders);
    
    // Base method with all BaseEntity ignores - other methods inherit from this
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "expectedDeliveryDate", ignore = true)
    @Mapping(target = "actualDeliveryDate", ignore = true)
    @Mapping(target = "totalEstimatedAmount", ignore = true)
    @Mapping(target = "trackingNumber", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "purchaseOrderItems", ignore = true)
    PurchaseOrder toEntity(CreatePurchaseOrderDTO purchaseOrderDto);

    @InheritConfiguration(name = "toEntity")
    void updatePurchaseOrderFromDto(UpdatePurchaseOrderDTO purchaseOrderDto, @MappingTarget PurchaseOrder purchaseOrder);
    
    @org.mapstruct.Named("statusToString")
    default String statusToString(com.store.mgmt.inventory.model.enums.PurchaseOrderStatus status) {
        return status != null ? status.name() : null;
    }
}