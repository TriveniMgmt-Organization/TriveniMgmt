package com.store.mgmt.inventory.mapper;
import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.inventory.model.dto.CreateSupplierDTO;
import com.store.mgmt.inventory.model.dto.SupplierDTO;
import com.store.mgmt.inventory.model.dto.UpdateSupplierDTO;
import com.store.mgmt.inventory.model.entity.Supplier;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", 
        config = BaseMapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SupplierMapper {
    @Mapping(target = "contactInfo", ignore = true) // Not in DTO or handled separately
    @Mapping(target = "phoneNumber", ignore = true) // Not in DTO or handled separately
    SupplierDTO toDto(Supplier supplier);
    List<SupplierDTO> toDtoList(List<Supplier> suppliers);
    
    // Base method with all BaseEntity ignores - other methods inherit from this
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "contactPerson", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "purchaseOrders", ignore = true)
    Supplier toEntity(CreateSupplierDTO createDTO);
    
    @InheritConfiguration(name = "toEntity")
    void updateSupplierFromDto(UpdateSupplierDTO updateDTO, @MappingTarget Supplier supplier);
}