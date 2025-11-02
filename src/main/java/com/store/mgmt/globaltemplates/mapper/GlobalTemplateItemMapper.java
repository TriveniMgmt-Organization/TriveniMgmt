package com.store.mgmt.globaltemplates.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.globaltemplates.model.dto.GlobalTemplateItemDTO;
import com.store.mgmt.globaltemplates.model.entity.GlobalTemplateItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        config = BaseMapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GlobalTemplateItemMapper {
    
    ObjectMapper objectMapper = new ObjectMapper();

    @Mapping(target = "templateId", source = "template.id")
    @Mapping(target = "data", source = "data")
    GlobalTemplateItemDTO toDto(GlobalTemplateItem entity);

    List<GlobalTemplateItemDTO> toDtoList(List<GlobalTemplateItem> entities);
}

