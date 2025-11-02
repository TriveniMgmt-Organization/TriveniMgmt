package com.store.mgmt.inventory.mapper;

import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.inventory.model.dto.*;
import com.store.mgmt.inventory.model.entity.Category;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        config = BaseMapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {

    CategoryDTO toDto(Category category);
    List<CategoryDTO> toDtoList(List<Category> categories);

    // Base method with all BaseEntity ignores - other methods inherit from this
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "active", ignore = true)
    Category toEntity(CreateCategoryDTO createDTO);

    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "active", ignore = true)
    void updateCategoryFromDto(UpdateCategoryDTO updateDTO, @MappingTarget Category category);
}