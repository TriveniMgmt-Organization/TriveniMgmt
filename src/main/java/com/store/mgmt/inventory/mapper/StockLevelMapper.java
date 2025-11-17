package com.store.mgmt.inventory.mapper;

import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.inventory.model.dto.StockLevelDTO;
import com.store.mgmt.inventory.model.entity.StockLevel;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        config = BaseMapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StockLevelMapper {

    StockLevelDTO toDto(StockLevel entity);
    
    List<StockLevelDTO> toDtoList(List<StockLevel> entities);
}

