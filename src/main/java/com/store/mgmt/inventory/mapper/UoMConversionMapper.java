package com.store.mgmt.inventory.mapper;

import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.inventory.model.dto.CreateUoMConversionDTO;
import com.store.mgmt.inventory.model.dto.UoMConversionDTO;
import com.store.mgmt.inventory.model.entity.UoMConversion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        config = BaseMapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UoMConversionMapper {

    @Mapping(source = "fromUom.id", target = "fromUomId")
    @Mapping(source = "toUom.id", target = "toUomId")
    @Mapping(target = "fromUom.description", ignore = true)
    @Mapping(target = "fromUom.conversionFactor", ignore = true)
    @Mapping(target = "toUom.description", ignore = true)
    @Mapping(target = "toUom.conversionFactor", ignore = true)
    @Mapping(target = "description", expression = "java(String.format(\"1 %s = %.2f %s\", conversion.getFromUom().getName(), conversion.getRatio(), conversion.getToUom().getName()))")
    UoMConversionDTO toDto(UoMConversion conversion);
    
    List<UoMConversionDTO> toDtoList(List<UoMConversion> conversions);

    // Base method with all BaseEntity ignores
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "fromUom", ignore = true)
    @Mapping(target = "toUom", ignore = true)
    UoMConversion toEntity(CreateUoMConversionDTO createDTO);
}

