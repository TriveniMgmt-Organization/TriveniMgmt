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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
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
    private static final int BATCH_SIZE = 50;
    

    public void applyTemplate(Organization org, String templateCode) {
        log.info("Applying template '{}' to organization '{}' (ID: {})", templateCode, org.getName(), org.getId());

        GlobalTemplate template = templateRepository.findByCode(templateCode)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateCode));

        if (!Boolean.TRUE.equals(template.getIsActive())) {
            throw new IllegalArgumentException("Template is not active: " + templateCode);
        }

        Set<GlobalTemplateItem> items = template.getItems();
        if (items == null || items.isEmpty()) {
            log.warn("Template '{}' has no items", templateCode);
            return;
        }

        Map<String, UUID> codeToId = new HashMap<>();
        int processed = 0, skipped = 0, errors = 0;
        int batchCount = 0;

        for (GlobalTemplateItem item : items) {
            try {
                JsonNode dataNode = item.getData();
                if (dataNode == null || dataNode.isNull() || (dataNode.isTextual() && dataNode.asText().trim().isEmpty())) {
                    log.warn("Item {} has no data, skipping", item.getId());
                    skipped++;
                    continue;
                }

                JsonNode data = dataNode.isTextual() ? objectMapper.readTree(dataNode.asText()) : dataNode;
                String entityType = item.getEntityType();
                String normalizedType = normalizeEntityType(entityType);

                if (normalizedType == null) {
                    log.warn("Invalid entityType '{}' for item {}", entityType, item.getId());
                    skipped++;
                    continue;
                }

                switch (normalizedType) {
                    case "Brand" -> copyBrand(org, data);
                    case "Category" -> copyCategory(org, data, codeToId);
                    case "UnitOfMeasure" -> copyUom(org, data, codeToId);
                    case "ProductTemplate" -> copyProductTemplate(org, data, codeToId);
                    case "TaxRule" -> copyTaxRule(org, data);
                    default -> {
                        log.debug("Skipping unsupported entity type: {}", normalizedType);
                        skipped++;
                    }
                }
                processed++;

            } catch (Exception e) {
                log.error("Error processing item {} (type: {}): {}", item.getId(), item.getEntityType(), e.getMessage(), e);
                errors++;
            }

            // Batch flush
            if (++batchCount % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.flush();
        entityManager.clear();

        log.info("Template '{}' applied: {} processed, {} skipped, {} errors", templateCode, processed, skipped, errors);

        if (processed == 0 && errors > 0) {
            throw new RuntimeException("Failed to apply template: " + errors + " errors");
        }
    }

    // ========================================================================
    // ENTITY COPIERS
    // ========================================================================

    private void copyBrand(Organization org, JsonNode data) {
        String name = getRequiredText(data, "name", "Brand");
        if (name == null) return;

        if (brandRepository.findByName(name).isPresent()) {
            log.debug("Brand '{}' exists globally, skipping", name);
            return;
        }

        Brand brand = new Brand();
        brand.setName(name);
        setIfPresent(data, "description", brand::setDescription);
        setIfPresent(data, "logoUrl", brand::setLogoUrl);
        setIfPresent(data, "website", brand::setWebsite);

        brandRepository.save(brand);
        log.info("Created global Brand: {}", name);
    }

    private void copyCategory(Organization org, JsonNode data, Map<String, UUID> codeToId) {
        String code = getRequiredText(data, "code", "Category");
        String name = getRequiredText(data, "name", "Category");
        if (code == null || name == null) return;

        if (categoryRepository.findByCodeAndOrganizationId(code, org.getId()).isPresent()) {
            log.debug("Category '{}' exists in org, skipping", code);
            return;
        }

        Category cat = new Category();
        cat.setOrganization(org);
        cat.setCode(code);
        cat.setName(name);
        setIfPresent(data, "description", cat::setDescription);
        cat.setActive(getBoolean(data, "isActive", true));

        // Parent reference
        String parentCode = getText(data, "parentCode");
        if (parentCode != null && codeToId.containsKey(parentCode)) {
            // Note: Category entity doesn't have a parent field currently
            // If parent category support is needed, add parent field to Category entity first
        }

        cat = categoryRepository.save(cat);
        codeToId.put(code, cat.getId());
        log.info("Created Category: {} ({})", name, code);
    }

    private void copyUom(Organization org, JsonNode data, Map<String, UUID> codeToId) {
        String code = getRequiredText(data, "code", "UnitOfMeasure");
        String name = getRequiredText(data, "name", "UnitOfMeasure");
        if (code == null || name == null) return;

        if (unitOfMeasureRepository.findByCodeAndOrganizationId(code, org.getId()).isPresent()) {
            log.debug("UOM '{}' exists in org, skipping", code);
            return;
        }

        UnitOfMeasure uom = new UnitOfMeasure();
        uom.setOrganization(org);
        uom.setCode(code);
        uom.setName(name);

        uom = unitOfMeasureRepository.save(uom);
        codeToId.put(code, uom.getId());
        log.info("Created UOM: {} ({})", name, code);
    }

    private void copyProductTemplate(Organization org, JsonNode data, Map<String, UUID> codeToId) {
        String name = getRequiredText(data, "name", "ProductTemplate");
        if (name == null) return;

        // Check if ProductTemplate with same name exists in organization
        boolean exists = productTemplateRepository.findByOrganizationId(org.getId()).stream()
                .anyMatch(pt -> name.equals(pt.getName()));
        if (exists) {
            log.debug("ProductTemplate '{}' exists, skipping", name);
            return;
        }

        // Required
        String uomCode = getRequiredText(data, "uomCode", "ProductTemplate");
        String skuPrefix = getRequiredText(data, "skuPrefix", "ProductTemplate");
        BigDecimal costPrice = getRequiredDecimal(data, "costPrice", "ProductTemplate");
        BigDecimal retailPrice = getRequiredDecimal(data, "retailPrice", "ProductTemplate");
        if (uomCode == null || skuPrefix == null || costPrice == null || retailPrice == null) return;

        // Resolve UOM
        UnitOfMeasure uom = resolveUom(org, uomCode, codeToId);
        if (uom == null) {
            log.warn("UOM '{}' not found for ProductTemplate '{}'", uomCode, name);
            return;
        }

        // Resolve Category
        Category category = null;
        String catCode = getText(data, "categoryCode");
        if (catCode != null) {
            category = resolveCategory(org, catCode, codeToId);
        }

        // Resolve Brand
        Brand brand = null;
        String brandName = getText(data, "brandName");
        if (brandName != null) {
            brand = brandRepository.findByName(brandName).orElse(null);
        }

        // Create ProductTemplate
        ProductTemplate pt = new ProductTemplate();
        pt.setOrganization(org);
        pt.setName(name);
        pt.setUnitOfMeasure(uom);
        pt.setCategory(category);
        pt.setBrand(brand);
        pt.setRequiresExpiry(getBoolean(data, "requiresExpiry", false));
        pt.setReorderPoint(getInt(data, "reorderPoint", 0));
        pt.setActive(true);
        String description = getText(data, "description");
        if (description != null) {
            pt.setDescription(description);
        }

        pt = productTemplateRepository.save(pt);
        log.info("Created ProductTemplate: {}", name);

        // Always create ProductVariant with generated SKU
        createProductVariant(org, pt, data, skuPrefix, costPrice, retailPrice);
    }

    private void createProductVariant(Organization org, ProductTemplate template, JsonNode data,
                                      String skuPrefix, BigDecimal costPrice, BigDecimal retailPrice) {

        String sku = generateUniqueSku(org, skuPrefix);
        if (productVariantRepository.findBySkuAndOrganizationId(sku, org.getId()).isPresent()) {
            log.debug("Generated SKU '{}' already exists, skipping variant", sku);
            return;
        }

        ProductVariant variant = new ProductVariant();
        variant.setOrganization(org);
        variant.setTemplate(template);
        variant.setSku(sku);
        variant.setCostPrice(costPrice);
        variant.setRetailPrice(retailPrice);
        variant.setActive(true);

        String barcode = getText(data, "barcode");
        if (barcode != null && !barcode.isEmpty()
                && productVariantRepository.findByBarcodeAndOrganizationId(barcode, org.getId()).isEmpty()) {
            variant.setBarcode(barcode);
        }

        productVariantRepository.save(variant);
        log.info("Created ProductVariant: {} (SKU: {})", template.getName(), sku);
    }

    private void copyTaxRule(Organization org, JsonNode data) {
        String countryCode = getRequiredText(data, "countryCode", "TaxRule");
        BigDecimal taxRate = getRequiredDecimal(data, "taxRate", "TaxRule");
        if (countryCode == null || taxRate == null) return;

        if (taxRuleRepository.findByOrganizationIdAndCountryCode(org.getId(), countryCode).isPresent()) {
            log.debug("TaxRule for '{}' exists, skipping", countryCode);
            return;
        }

        TaxRule rule = new TaxRule();
        rule.setOrganization(org);
        rule.setCountryCode(countryCode);
        rule.setTaxRate(taxRate);
        setIfPresent(data, "description", rule::setDescription);

        taxRuleRepository.save(rule);
        log.info("Created TaxRule: {}% for {}", taxRate, countryCode);
    }

    // ========================================================================
    // UTILITIES
    // ========================================================================

    private UnitOfMeasure resolveUom(Organization org, String code, Map<String, UUID> map) {
        if (map.containsKey(code)) {
            return unitOfMeasureRepository.findByIdAndOrganizationId(map.get(code), org.getId()).orElse(null);
        }
        return unitOfMeasureRepository.findByCodeAndOrganizationId(code, org.getId()).orElse(null);
    }

    private Category resolveCategory(Organization org, String code, Map<String, UUID> map) {
        if (map.containsKey(code)) {
            return categoryRepository.findByIdAndOrganizationId(map.get(code), org.getId()).orElse(null);
        }
        return categoryRepository.findByCodeAndOrganizationId(code, org.getId()).orElse(null);
    }

    private String generateUniqueSku(Organization org, String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) return null;
        String base = prefix.toUpperCase().replaceAll("[^A-Z0-9]", "");
        int seq = 1;
        String sku;
        do {
            sku = base + "-" + String.format("%04d", seq++);
        } while (productVariantRepository.findBySkuAndOrganizationId(sku, org.getId()).isPresent());
        return sku;
    }

    private String normalizeEntityType(String entityType) {
        if (entityType == null || entityType.trim().isEmpty()) return null;
        String upper = entityType.trim().toUpperCase();
        return switch (upper) {
            case "UOM", "UNIT_OF_MEASURE", "UNITOFMEASURE" -> "UnitOfMeasure";
            case "PRODUCT_TEMPLATE", "PRODUCTTEMPLATE" -> "ProductTemplate";
            case "TAX_RULE", "TAXRULE" -> "TaxRule";
            case "BRAND" -> "Brand";
            case "CATEGORY" -> "Category";
            default -> upper.substring(0, 1) + upper.substring(1).toLowerCase();
        };
    }

    // Helper methods
    private String getRequiredText(JsonNode node, String field, String type) {
        if (!node.has(field) || node.get(field).isNull()) {
            log.warn("{} missing required field: {}", type, field);
            return null;
        }
        String val = node.get(field).asText().trim();
        if (val.isEmpty()) {
            log.warn("{} field '{}' is empty", type, field);
            return null;
        }
        return val;
    }

    private String getText(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText().trim() : null;
    }

    private BigDecimal getRequiredDecimal(JsonNode node, String field, String type) {
        if (!node.has(field) || node.get(field).isNull()) {
            log.warn("{} missing required field: {}", type, field);
            return null;
        }
        try {
            return new BigDecimal(node.get(field).asText());
        } catch (Exception e) {
            log.warn("Invalid {} value for field '{}': {}", type, field, e.getMessage());
            return null;
        }
    }

    private boolean getBoolean(JsonNode node, String field, boolean defaultValue) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asBoolean() : defaultValue;
    }

    private int getInt(JsonNode node, String field, int defaultValue) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asInt() : defaultValue;
    }

    private <T> void setIfPresent(JsonNode node, String field, Consumer<String> setter) {
        String val = getText(node, field);
        if (val != null) setter.accept(val);
    }
}