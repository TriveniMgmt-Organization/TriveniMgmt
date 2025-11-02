package com.store.mgmt.common.mapper;

import org.mapstruct.MapperConfig;

/**
 * Central configuration for MapStruct mappers.
 * 
 * IMPORTANT LIMITATION: MapStruct does NOT support sharing @Mapping annotations 
 * across mapper interfaces. Each mapper must define BaseEntity ignores once in 
 * their toEntity method, then use @InheritConfiguration on update methods.
 * 
 * BaseEntity fields (id, createdAt, createdBy, updatedAt, updatedBy, deletedAt, deletedBy)
 * should be ignored when mapping from DTOs to entities since they are managed by JPA/Hibernate.
 * 
 * Pattern to follow in each mapper:
 * 1. Define BaseEntity ignores ONCE in toEntity method
 * 2. Use @InheritConfiguration(name = "toEntity") on update methods
 * 
 * See _BaseEntityMappingTemplate.java for the exact annotations to copy.
 */
@MapperConfig(
    componentModel = "spring"
)
public interface BaseMapperConfig {
    // Configuration shared across all mappers
    // Individual mappers will define their own ignore mappings and use @InheritConfiguration
    // to share them within the same mapper interface
}

