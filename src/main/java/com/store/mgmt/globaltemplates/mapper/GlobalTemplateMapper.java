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
    
    @Mapping(target = "items", expression = "java(mapItems(template.getItems()))")
    GlobalTemplateDTO toDto(GlobalTemplate template);
    
    List<GlobalTemplateDTO> toDtoList(List<GlobalTemplate> templates);
    
    default java.util.List<com.store.mgmt.globaltemplates.model.dto.GlobalTemplateItemDTO> mapItems(java.util.Set<com.store.mgmt.globaltemplates.model.entity.GlobalTemplateItem> items) {
        if (items == null || items.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        GlobalTemplateItemMapper itemMapper = org.mapstruct.factory.Mappers.getMapper(GlobalTemplateItemMapper.class);
        java.util.List<com.store.mgmt.globaltemplates.model.entity.GlobalTemplateItem> itemList = new java.util.LinkedList<>();
        itemList.addAll(items);
        return itemMapper.toDtoList(itemList);
    }

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

