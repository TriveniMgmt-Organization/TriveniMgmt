package com.store.mgmt.globaltemplates.mapper;

import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.globaltemplates.model.dto.*;
import com.store.mgmt.globaltemplates.model.entity.GlobalTemplate;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        config = BaseMapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = GlobalTemplateItemMapper.class)
public interface GlobalTemplateMapper {

    GlobalTemplateDTO toDto(GlobalTemplate template);
    List<GlobalTemplateDTO> toDtoList(List<GlobalTemplate> templates);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "items", ignore = true)
    GlobalTemplate toEntity(CreateGlobalTemplateDTO createDTO);

    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "code", ignore = true) // Code should not be updated
    void updateTemplateFromDto(UpdateGlobalTemplateDTO updateDTO, @MappingTarget GlobalTemplate template);
}

