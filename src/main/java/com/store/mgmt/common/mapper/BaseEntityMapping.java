package com.store.mgmt.common.mapper;

/**
 * Centralized constant for BaseEntity field ignore mappings.
 * 
 * Since MapStruct doesn't support sharing @Mapping annotations across interfaces,
 * use this class as a reference when defining mapper methods.
 * 
 * Each mapper should copy these annotations to their toEntity method:
 * 
 * @Mapping(target = "id", ignore = true)
 * @Mapping(target = "createdAt", ignore = true)
 * @Mapping(target = "createdBy", ignore = true)
 * @Mapping(target = "updatedAt", ignore = true)
 * @Mapping(target = "updatedBy", ignore = true)
 * @Mapping(target = "deletedAt", ignore = true)
 * @Mapping(target = "deletedBy", ignore = true)
 * 
 * Then use @InheritConfiguration(name = "toEntity") on update methods.
 */
public final class BaseEntityMapping {
    
    private BaseEntityMapping() {
        // Utility class - no instantiation
    }
    
    /**
     * String array of BaseEntity field names that should be ignored.
     * Can be used for documentation or code generation purposes.
     */
    public static final String[] BASE_ENTITY_IGNORE_FIELDS = {
        "id",
        "createdAt",
        "createdBy",
        "updatedAt",
        "updatedBy",
        "deletedAt",
        "deletedBy"
    };
    
    /**
     * Complete @Mapping annotations for BaseEntity fields.
     * Copy these annotations to your mapper's toEntity method.
     */
    public static final String BASE_ENTITY_IGNORE_MAPPINGS = 
        "@Mapping(target = \"id\", ignore = true)\n" +
        "@Mapping(target = \"createdAt\", ignore = true)\n" +
        "@Mapping(target = \"createdBy\", ignore = true)\n" +
        "@Mapping(target = \"updatedAt\", ignore = true)\n" +
        "@Mapping(target = \"updatedBy\", ignore = true)\n" +
        "@Mapping(target = \"deletedAt\", ignore = true)\n" +
        "@Mapping(target = \"deletedBy\", ignore = true)";
}

