package com.store.mgmt.globaltemplates.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.mgmt.common.exception.ResourceNotFoundException;
import com.store.mgmt.globaltemplates.model.entity.GlobalTemplate;
import com.store.mgmt.globaltemplates.model.entity.GlobalTemplateItem;
import com.store.mgmt.globaltemplates.repository.GlobalTemplateRepository;
import com.store.mgmt.inventory.model.entity.Brand;
import com.store.mgmt.inventory.model.entity.Category;
import com.store.mgmt.inventory.model.entity.UnitOfMeasure;
import com.store.mgmt.inventory.model.entity.ProductTemplate;
import com.store.mgmt.inventory.model.entity.ProductVariant;
import com.store.mgmt.inventory.model.entity.TaxRule;
import com.store.mgmt.inventory.repository.BrandRepository;
import com.store.mgmt.inventory.repository.CategoryRepository;
import com.store.mgmt.inventory.repository.UnitOfMeasureRepository;
import com.store.mgmt.inventory.repository.ProductTemplateRepository;
import com.store.mgmt.inventory.repository.ProductVariantRepository;
import com.store.mgmt.inventory.repository.TaxRuleRepository;
import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.repository.OrganizationRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class TemplateCopyService {
    
    private final GlobalTemplateRepository templateRepository;
    private final OrganizationRepository organizationRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final ProductTemplateRepository productTemplateRepository;
    private final ProductVariantRepository productVariantRepository;
    private final TaxRuleRepository taxRuleRepository;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;
    
    public TemplateCopyService(
            GlobalTemplateRepository templateRepository,
            OrganizationRepository organizationRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            UnitOfMeasureRepository unitOfMeasureRepository,
            ProductTemplateRepository productTemplateRepository,
            ProductVariantRepository productVariantRepository,
            TaxRuleRepository taxRuleRepository,
            ObjectMapper objectMapper,
            EntityManager entityManager) {
        this.templateRepository = templateRepository;
        this.organizationRepository = organizationRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.productTemplateRepository = productTemplateRepository;
        this.productVariantRepository = productVariantRepository;
        this.taxRuleRepository = taxRuleRepository;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
    }
    
    /**
     * Normalizes entity type names to match Java class names.
     * 
     * Note: TemplateSeeder now normalizes entity types when saving to the database,
     * so this normalization is primarily for backward compatibility with existing data
     * that might have old entity type names (e.g., "UOM", "PRODUCT_TEMPLATE").
     * 
     * Handles variations like:
     * - "UOM", "UNIT_OF_MEASURE", "UnitOfMeasure" -> "UnitOfMeasure"
     * - "PRODUCT_TEMPLATE", "PRODUCTTEMPLATE", "ProductTemplate" -> "ProductTemplate"
     * - "TAX_RULE", "TAXRULE", "TaxRule" -> "TaxRule"
     * - "BRAND", "Brand" -> "Brand"
     * - "CATEGORY", "Category" -> "Category"
     */
    private String normalizeEntityType(String entityType) {
        if (entityType == null || entityType.trim().isEmpty()) {
            return null;
        }
        
        String normalized = entityType.trim();
        
        // Convert to uppercase for comparison
        String upper = normalized.toUpperCase();
        
        // Handle variations
        if (upper.equals("UOM") || upper.equals("UNIT_OF_MEASURE") || upper.equals("UNITOFMEASURE")) {
            return "UnitOfMeasure";
        } else if (upper.equals("PRODUCT_TEMPLATE") || upper.equals("PRODUCTTEMPLATE")) {
            return "ProductTemplate";
        } else if (upper.equals("TAX_RULE") || upper.equals("TAXRULE")) {
            return "TaxRule";
        } else if (upper.equals("BRAND")) {
            return "Brand";
        } else if (upper.equals("CATEGORY")) {
            return "Category";
        }
        
        // If it's already in proper camelCase/PascalCase, return as-is (capitalize first letter)
        // Handle camelCase: "unitOfMeasure" -> "UnitOfMeasure"
        if (normalized.length() > 0) {
            return normalized.substring(0, 1).toUpperCase() + normalized.substring(1);
        }
        
        return normalized;
    }
    
    public void applyTemplate(Organization org, String templateCode) {
        log.info("Applying template '{}' to organization '{}' (ID: {})", templateCode, org.getName(), org.getId());
        
        GlobalTemplate template = templateRepository.findByCode(templateCode)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with code: " + templateCode));
        
        if (!template.getIsActive()) {
            throw new IllegalArgumentException("Template is not active: " + templateCode);
        }
        
        // Ensure items are loaded - items should be loaded via JOIN FETCH in findByCode
        // But force initialization just to be safe
        if (template.getItems() == null) {
            log.warn("Template '{}' has null items collection", templateCode);
            return;
        }
        
        // Force initialization of lazy collection
        int itemCount = template.getItems().size();
        log.info("Template '{}' has {} items to process", templateCode, itemCount);
        
        if (itemCount == 0) {
            log.warn("Template '{}' has no items to apply", templateCode);
            return;
        }
        
        Map<String, UUID> codeToId = new HashMap<>(); // For parent references (e.g., parent categories)
        int processedCount = 0;
        int skippedCount = 0;
        int errorCount = 0;
        
        for (GlobalTemplateItem item : template.getItems()) {
            try {
                String entityType = item.getEntityType();
                log.debug("Processing item {} with entity type: {}", item.getId(), entityType);
                
                if (item.getData() == null || item.getData().trim().isEmpty()) {
                    log.warn("Item {} has no data, skipping", item.getId());
                    skippedCount++;
                    continue;
                }
                
                JsonNode data = objectMapper.readTree(item.getData());
                
                // Normalize entity type to match Java class names
                String normalizedType = normalizeEntityType(entityType);
                
                if (normalizedType == null) {
                    log.warn("Item {} has null or empty entity type, skipping", item.getId());
                    skippedCount++;
                    continue;
                }
                
                switch (normalizedType) {
                    case "Brand" -> {
                        copyBrand(org, data, codeToId);
                        processedCount++;
                    }
                    case "Category" -> {
                        copyCategory(org, data, codeToId);
                        processedCount++;
                    }
                    case "UnitOfMeasure" -> {
                        copyUom(org, data, codeToId);
                        processedCount++;
                    }
                    case "ProductTemplate" -> {
                        copyProductTemplate(org, data, codeToId);
                        processedCount++;
                    }
                    case "TaxRule" -> {
                        copyTaxRule(org, data, codeToId);
                        processedCount++;
                    }
                    default -> {
                        log.warn("Unknown or unsupported entity type '{}' (normalized: '{}') for item {}, skipping", 
                                entityType, normalizedType, item.getId());
                        skippedCount++;
                    }
                }
            } catch (Exception e) {
                log.error("Error processing template item {} (entityType: {}): {}", 
                        item.getId(), item.getEntityType(), e.getMessage(), e);
                errorCount++;
                // Don't throw - continue processing other items
            }
        }
        
        // Flush all changes to database
        entityManager.flush();
        
        log.info("Template '{}' applied to organization '{}' (ID: {}): {} processed, {} skipped, {} errors", 
                templateCode, org.getName(), org.getId(), processedCount, skippedCount, errorCount);
        
        if (processedCount == 0) {
            if (errorCount > 0) {
                log.error("Failed to process any items for template '{}'. Check error logs above.", templateCode);
                throw new RuntimeException("Failed to process template items. " + errorCount + " errors occurred.");
            } else {
                log.warn("No items were processed for template '{}'. All items may have been skipped or template may be empty.", templateCode);
            }
        } else {
            log.info("Successfully created {} entities for organization '{}' from template '{}'", 
                    processedCount, org.getName(), templateCode);
        }
    }
    
    private void copyBrand(Organization org, JsonNode data, Map<String, UUID> map) {
        if (!data.has("name") || data.get("name").isNull()) {
            log.warn("Brand data missing 'name' field, skipping");
            return;
        }
        
        String name = data.get("name").asText().trim();
        if (name.isEmpty()) {
            log.warn("Brand name is empty, skipping");
            return;
        }
        
        // Check if brand already exists (brands are global, not org-specific)
        if (brandRepository.findByName(name).isPresent()) {
            log.debug("Brand '{}' already exists, skipping", name);
            return;
        }
        
        Brand brand = new Brand();
        brand.setName(name);
        
        if (data.has("description") && !data.get("description").isNull()) {
            brand.setDescription(data.get("description").asText());
        }
        if (data.has("logoUrl") && !data.get("logoUrl").isNull()) {
            brand.setLogoUrl(data.get("logoUrl").asText());
        }
        if (data.has("website") && !data.get("website").isNull()) {
            brand.setWebsite(data.get("website").asText());
        }
        
        brand = brandRepository.save(brand);
        log.info("Created brand: {} for organization: {}", brand.getName(), org.getName());
    }
    
    private void copyCategory(Organization org, JsonNode data, Map<String, UUID> map) {
        if (!data.has("code") || data.get("code").isNull()) {
            log.warn("Category data missing 'code' field, skipping");
            return;
        }
        if (!data.has("name") || data.get("name").isNull()) {
            log.warn("Category data missing 'name' field, skipping");
            return;
        }
        
        String code = data.get("code").asText().trim();
        String name = data.get("name").asText().trim();
        
        if (code.isEmpty() || name.isEmpty()) {
            log.warn("Category code or name is empty, skipping");
            return;
        }
        
        // Check if category already exists
        if (categoryRepository.findByCodeAndOrganizationId(code, org.getId()).isPresent()) {
            log.debug("Category '{}' already exists in organization {}, skipping", code, org.getName());
            return;
        }
        
        Category cat = new Category();
        cat.setOrganization(org);
        cat.setCode(code);
        cat.setName(name);
        
        if (data.has("description") && !data.get("description").isNull()) {
            cat.setDescription(data.get("description").asText());
        }
        if (data.has("isActive") && !data.get("isActive").isNull()) {
            cat.setActive(data.get("isActive").asBoolean());
        } else {
            cat.setActive(true); // Default to active
        }
        
        // Handle parent category reference if needed
        // Note: This assumes parent categories are created before child categories in the template
        String parentCode = data.has("parent_code") && !data.get("parent_code").isNull() 
                ? data.get("parent_code").asText().trim() : null;
        if (parentCode != null && !parentCode.isEmpty() && map.containsKey(parentCode)) {
            UUID parentId = map.get(parentCode);
            // Assuming Category has a parent reference field - adjust if needed
            // cat.setParentId(parentId);
            log.debug("Parent category reference for '{}': {}", code, parentId);
        }
        
        cat = categoryRepository.save(cat);
        map.put(code, cat.getId());
        log.info("Created category: {} ({}) for organization: {}", name, code, org.getName());
    }
    
    private void copyUom(Organization org, JsonNode data, Map<String, UUID> map) {
        if (!data.has("code") || data.get("code").isNull()) {
            log.warn("UOM data missing 'code' field, skipping");
            return;
        }
        if (!data.has("name") || data.get("name").isNull()) {
            log.warn("UOM data missing 'name' field, skipping");
            return;
        }
        
        String code = data.get("code").asText().trim();
        String name = data.get("name").asText().trim();
        
        if (code.isEmpty() || name.isEmpty()) {
            log.warn("UOM code or name is empty, skipping");
            return;
        }
        
        // Check if UOM already exists
        if (unitOfMeasureRepository.findByCodeAndOrganizationId(code, org.getId()).isPresent()) {
            log.debug("Unit of Measure '{}' already exists in organization {}, skipping", code, org.getName());
            return;
        }
        
        UnitOfMeasure uom = new UnitOfMeasure();
        uom.setOrganization(org);
        uom.setCode(code);
        uom.setName(name);
        
        // Note: Description might not be in entity, adjust if needed
        // if (data.has("description") && !data.get("description").isNull()) {
        //     uom.setDescription(data.get("description").asText());
        // }
        
        uom = unitOfMeasureRepository.save(uom);
        map.put(code, uom.getId());
        log.info("Created unit of measure: {} ({}) for organization: {}", name, code, org.getName());
    }
    
    private void copyProductTemplate(Organization org, JsonNode data, Map<String, UUID> map) {
        if (!data.has("name") || data.get("name").isNull()) {
            log.warn("ProductTemplate data missing 'name' field, skipping");
            return;
        }
        
        String name = data.get("name").asText().trim();
        if (name.isEmpty()) {
            log.warn("ProductTemplate name is empty, skipping");
            return;
        }
        
        // Check if product template already exists
        // Note: We check by name + organization since there's no unique code for products
        if (productTemplateRepository.findByOrganizationId(org.getId()).stream()
                .anyMatch(pt -> pt.getName().equalsIgnoreCase(name))) {
            log.debug("ProductTemplate '{}' already exists in organization {}, skipping", name, org.getName());
            return;
        }
        
        // Required fields
        if (!data.has("unitOfMeasureId") && !data.has("unitOfMeasureCode") && !data.has("uomCode")) {
            log.warn("ProductTemplate data missing 'unitOfMeasureId' or 'unitOfMeasureCode'/'uomCode' field, skipping");
            return;
        }
        
        ProductTemplate productTemplate = new ProductTemplate();
        productTemplate.setOrganization(org);
        productTemplate.setName(name);
        
        if (data.has("description") && !data.get("description").isNull()) {
            productTemplate.setDescription(data.get("description").asText());
        }
        
        // Set UnitOfMeasure - try by ID first, then by code
        UnitOfMeasure uom = null;
        if (data.has("unitOfMeasureId") && !data.get("unitOfMeasureId").isNull()) {
            UUID uomId = UUID.fromString(data.get("unitOfMeasureId").asText());
            uom = unitOfMeasureRepository.findByIdAndOrganizationId(uomId, org.getId())
                    .orElse(null);
        } else {
            String uomCode = data.has("unitOfMeasureCode") && !data.get("unitOfMeasureCode").isNull()
                    ? data.get("unitOfMeasureCode").asText().trim()
                    : data.has("uomCode") && !data.get("uomCode").isNull()
                        ? data.get("uomCode").asText().trim()
                        : null;
            if (uomCode != null && map.containsKey(uomCode)) {
                uom = unitOfMeasureRepository.findByIdAndOrganizationId(map.get(uomCode), org.getId()).orElse(null);
            } else if (uomCode != null) {
                uom = unitOfMeasureRepository.findByCodeAndOrganizationId(uomCode, org.getId()).orElse(null);
            }
        }
        
        if (uom == null) {
            log.warn("ProductTemplate '{}' references invalid UnitOfMeasure, skipping", name);
            return;
        }
        productTemplate.setUnitOfMeasure(uom);
        
        // Set Category - optional
        if (data.has("categoryId") && !data.get("categoryId").isNull()) {
            UUID categoryId = UUID.fromString(data.get("categoryId").asText());
            Category category = categoryRepository.findByIdAndOrganizationId(categoryId, org.getId()).orElse(null);
            if (category != null) {
                productTemplate.setCategory(category);
            }
        } else if (data.has("categoryCode") && !data.get("categoryCode").isNull()) {
            String categoryCode = data.get("categoryCode").asText().trim();
            if (map.containsKey(categoryCode)) {
                Category category = categoryRepository.findByIdAndOrganizationId(map.get(categoryCode), org.getId()).orElse(null);
                if (category != null) {
                    productTemplate.setCategory(category);
                }
            } else {
                Category category = categoryRepository.findByCodeAndOrganizationId(categoryCode, org.getId()).orElse(null);
                if (category != null) {
                    productTemplate.setCategory(category);
                }
            }
        }
        
        // Set Brand - optional
        if (data.has("brandId") && !data.get("brandId").isNull()) {
            UUID brandId = UUID.fromString(data.get("brandId").asText());
            Brand brand = brandRepository.findById(brandId).orElse(null);
            if (brand != null) {
                productTemplate.setBrand(brand);
            }
        } else if (data.has("brandName") && !data.get("brandName").isNull()) {
            String brandName = data.get("brandName").asText().trim();
            Brand brand = brandRepository.findByName(brandName).orElse(null);
            if (brand != null) {
                productTemplate.setBrand(brand);
            }
        }
        
        if (data.has("imageUrl") && !data.get("imageUrl").isNull()) {
            productTemplate.setImageUrl(data.get("imageUrl").asText());
        }
        if (data.has("reorderPoint") && !data.get("reorderPoint").isNull()) {
            productTemplate.setReorderPoint(data.get("reorderPoint").asInt());
        }
        if (data.has("requiresExpiry") && !data.get("requiresExpiry").isNull()) {
            productTemplate.setRequiresExpiry(data.get("requiresExpiry").asBoolean());
        }
        if (data.has("isActive") && !data.get("isActive").isNull()) {
            productTemplate.setActive(data.get("isActive").asBoolean());
        } else {
            productTemplate.setActive(true); // Default to active
        }
        
        productTemplate = productTemplateRepository.save(productTemplate);
        log.info("Created product template: {} for organization: {}", productTemplate.getName(), org.getName());
        
        // Check if ProductTemplate data includes SKU/variant information
        // If so, create a ProductVariant linked to this template
        if (data.has("sku") && !data.get("sku").isNull()) {
            copyProductVariant(org, productTemplate, data);
        }
    }
    
    private void copyProductVariant(Organization org, ProductTemplate template, JsonNode data) {
        String sku = data.get("sku").asText().trim();
        if (sku.isEmpty()) {
            log.warn("ProductVariant SKU is empty for template '{}', skipping variant creation", template.getName());
            return;
        }
        
        // Check if variant already exists
        if (productVariantRepository.findBySkuAndOrganizationId(sku, org.getId()).isPresent()) {
            log.debug("ProductVariant with SKU '{}' already exists in organization {}, skipping", sku, org.getName());
            return;
        }
        
        // Required fields: sku, costPrice, retailPrice
        if (!data.has("costPrice") || data.get("costPrice").isNull()) {
            log.warn("ProductVariant data missing 'costPrice' field for SKU '{}', skipping", sku);
            return;
        }
        if (!data.has("retailPrice") || data.get("retailPrice").isNull()) {
            log.warn("ProductVariant data missing 'retailPrice' field for SKU '{}', skipping", sku);
            return;
        }
        
        ProductVariant variant = new ProductVariant();
        variant.setOrganization(org);
        variant.setTemplate(template);
        variant.setSku(sku);
        
        try {
            java.math.BigDecimal costPrice = new java.math.BigDecimal(data.get("costPrice").asText());
            variant.setCostPrice(costPrice);
            
            java.math.BigDecimal retailPrice = new java.math.BigDecimal(data.get("retailPrice").asText());
            variant.setRetailPrice(retailPrice);
        } catch (Exception e) {
            log.warn("Invalid price values for SKU '{}', skipping: {}", sku, e.getMessage());
            return;
        }
        
        if (data.has("barcode") && !data.get("barcode").isNull()) {
            String barcode = data.get("barcode").asText().trim();
            if (!barcode.isEmpty()) {
                // Check if barcode already exists
                if (productVariantRepository.findByBarcodeAndOrganizationId(barcode, org.getId()).isPresent()) {
                    log.debug("ProductVariant with barcode '{}' already exists in organization {}, skipping barcode", barcode, org.getName());
                } else {
                    variant.setBarcode(barcode);
                }
            }
        }
        
        if (data.has("isActive") && !data.get("isActive").isNull()) {
            variant.setActive(data.get("isActive").asBoolean());
        } else {
            variant.setActive(true); // Default to active
        }
        
        // Handle attribute values if present
        if (data.has("attributeValues") && !data.get("attributeValues").isNull() && data.get("attributeValues").isObject()) {
            java.util.Map<String, String> attributeValues = new java.util.HashMap<>();
            data.get("attributeValues").fields().forEachRemaining(entry -> {
                attributeValues.put(entry.getKey(), entry.getValue().asText());
            });
            if (!attributeValues.isEmpty()) {
                variant.setAttributeValues(attributeValues);
            }
        }
        
        variant = productVariantRepository.save(variant);
        log.info("Created product variant: {} (SKU: {}) for template: {} in organization: {}", 
                variant.getId(), sku, template.getName(), org.getName());
    }
    
    private void copyTaxRule(Organization org, JsonNode data, Map<String, UUID> map) {
        if (!data.has("countryCode") || data.get("countryCode").isNull()) {
            log.warn("TaxRule data missing 'countryCode' field, skipping");
            return;
        }
        if (!data.has("taxRate") || data.get("taxRate").isNull()) {
            log.warn("TaxRule data missing 'taxRate' field, skipping");
            return;
        }
        
        String countryCode = data.get("countryCode").asText().trim();
        if (countryCode.isEmpty()) {
            log.warn("TaxRule countryCode is empty, skipping");
            return;
        }
        
        // Check if tax rule already exists
        if (taxRuleRepository.findByOrganizationIdAndCountryCode(org.getId(), countryCode).isPresent()) {
            log.debug("TaxRule for country '{}' already exists in organization {}, skipping", countryCode, org.getName());
            return;
        }
        
        TaxRule taxRule = new TaxRule();
        taxRule.setOrganization(org);
        taxRule.setCountryCode(countryCode);
        
        try {
            java.math.BigDecimal taxRate = new java.math.BigDecimal(data.get("taxRate").asText());
            taxRule.setTaxRate(taxRate);
        } catch (Exception e) {
            log.warn("Invalid taxRate value for country '{}', skipping: {}", countryCode, e.getMessage());
            return;
        }
        
        if (data.has("description") && !data.get("description").isNull()) {
            taxRule.setDescription(data.get("description").asText());
        }
        
        taxRule = taxRuleRepository.save(taxRule);
        log.info("Created tax rule: {} ({}) for organization: {}", countryCode, taxRule.getTaxRate(), org.getName());
    }
}

