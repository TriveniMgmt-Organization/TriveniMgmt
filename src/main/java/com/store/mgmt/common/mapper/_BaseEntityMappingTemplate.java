package com.store.mgmt.common.mapper;

/**
 * TEMPLATE FILE - Copy these annotations to your mapper's toEntity method.
 * 
 * This file serves as a template for BaseEntity field ignore mappings.
 * Copy the annotations below to your mapper interface's toEntity method.
 * 
 * Then use @InheritConfiguration(name = "toEntity") on your update methods.
 * 
 * ============================================================================
 * 
 * // Base method with all BaseEntity ignores - other methods inherit from this
 * @Mapping(target = "id", ignore = true)
 * @Mapping(target = "createdAt", ignore = true)
 * @Mapping(target = "createdBy", ignore = true)
 * @Mapping(target = "updatedAt", ignore = true)
 * @Mapping(target = "updatedBy", ignore = true)
 * @Mapping(target = "deletedAt", ignore = true)
 * @Mapping(target = "deletedBy", ignore = true)
 * Entity toEntity(CreateDTO dto);
 * 
 * @InheritConfiguration(name = "toEntity")
 * void updateFromDto(UpdateDTO dto, @MappingTarget Entity entity);
 * 
 * ============================================================================
 * 
 * NOTE: This is a template file only. It's not compiled or used directly.
 * MapStruct doesn't support sharing @Mapping annotations across interfaces.
 */

