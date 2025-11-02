package com.store.mgmt.inventory.mapper;

import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.inventory.model.dto.BatchLotDTO;
import com.store.mgmt.inventory.model.dto.CreateBatchLotDTO;
import com.store.mgmt.inventory.model.entity.BatchLot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        config = BaseMapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BatchLotMapper {

    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(target = "supplier.contactInfo", ignore = true) // Not mapped
    @Mapping(target = "supplier.phoneNumber", ignore = true) // Not mapped
    BatchLotDTO toDto(BatchLot batchLot);
    List<BatchLotDTO> toDtoList(List<BatchLot> batchLots);

    // Base method with all BaseEntity ignores
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    BatchLot toEntity(CreateBatchLotDTO createDTO);
}

