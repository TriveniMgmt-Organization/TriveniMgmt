package com.store.mgmt.globaltemplates.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.store.mgmt.common.exception.ResourceNotFoundException;
import com.store.mgmt.globaltemplates.mapper.GlobalTemplateMapper;
import com.store.mgmt.globaltemplates.model.dto.*;
import com.store.mgmt.globaltemplates.model.entity.GlobalTemplate;
import com.store.mgmt.globaltemplates.model.entity.GlobalTemplateItem;
import com.store.mgmt.globaltemplates.repository.GlobalTemplateItemRepository;
import com.store.mgmt.globaltemplates.repository.GlobalTemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class GlobalTemplateServiceImpl implements GlobalTemplateService {
    
    private final GlobalTemplateRepository templateRepository;
    private final GlobalTemplateItemRepository itemRepository;
    private final GlobalTemplateMapper templateMapper;
    private final ObjectMapper objectMapper;
    
    public GlobalTemplateServiceImpl(
            GlobalTemplateRepository templateRepository,
            GlobalTemplateItemRepository itemRepository,
            GlobalTemplateMapper templateMapper,
            ObjectMapper objectMapper) {
        this.templateRepository = templateRepository;
        this.itemRepository = itemRepository;
        this.templateMapper = templateMapper;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public GlobalTemplateDTO createTemplate(CreateGlobalTemplateDTO createDTO) {
        log.info("Creating global template with code: {}", createDTO.getCode());
        
        // Check if template with code already exists
        if (templateRepository.findByCode(createDTO.getCode()).isPresent()) {
            throw new IllegalArgumentException("Template with code '" + createDTO.getCode() + "' already exists.");
        }
        
        GlobalTemplate template = templateMapper.toEntity(createDTO);
        template = templateRepository.save(template);
        log.info("Global template created with ID: {}", template.getId());
        
        // Reload with items for DTO mapping
        template = templateRepository.findByIdWithItems(template.getId())
                .orElse(template);
        return templateMapper.toDto(template);
    }
    
    @Override
    @Transactional(readOnly = true)
    public GlobalTemplateDTO getTemplateById(UUID templateId) {
        log.debug("Fetching global template with ID: {}", templateId);
        GlobalTemplate template = templateRepository.findByIdWithItems(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Global template not found with ID: " + templateId));
        
        // Items are already loaded via JOIN FETCH in repository query
        // Force initialization of the collection while still in transaction
        if (template.getItems() != null) {
            int itemCount = template.getItems().size();
            log.debug("Template {} has {} items loaded", templateId, itemCount);
        }
        
        return templateMapper.toDto(template);
    }
    
    @Override
    @Transactional(readOnly = true)
    public GlobalTemplateDTO getTemplateByCode(String code) {
        log.debug("Fetching global template with code: {}", code);
        GlobalTemplate template = templateRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Global template not found with code: " + code));
        
        // Items are already loaded via JOIN FETCH in repository query
        return templateMapper.toDto(template);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<GlobalTemplateDTO> getAllTemplates() {
        log.debug("Fetching all global templates");
        List<GlobalTemplate> templates = templateRepository.findAll(); // Already uses LEFT JOIN FETCH
        List<GlobalTemplateDTO> dtos = templateMapper.toDtoList(templates);
        log.debug("Mapped {} templates with items", dtos.size());
        return dtos;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<GlobalTemplateDTO> getActiveTemplates() {
        log.debug("Fetching all active global templates");
        List<GlobalTemplate> templates = templateRepository.findAllActive();
        return templateMapper.toDtoList(templates);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<GlobalTemplateDTO> getTemplatesByType(String type) {
        log.debug("Fetching global templates by type: {}", type);
        List<GlobalTemplate> templates = templateRepository.findByType(type);
        return templateMapper.toDtoList(templates);
    }
    
    @Override
    public GlobalTemplateDTO updateTemplate(UUID templateId, UpdateGlobalTemplateDTO updateDTO) {
        log.info("Updating global template with ID: {}", templateId);
        GlobalTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Global template not found with ID: " + templateId));
        
        templateMapper.updateTemplateFromDto(updateDTO, template);
        template = templateRepository.save(template);
        log.info("Global template updated with ID: {}", templateId);
        
        // Reload with items for DTO mapping
        template = templateRepository.findByIdWithItems(templateId)
                .orElse(template);
        
        return templateMapper.toDto(template);
    }
    
    @Override
    public void deleteTemplate(UUID templateId) {
        log.info("Deleting global template with ID: {}", templateId);
        GlobalTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Global template not found with ID: " + templateId));
        
        templateRepository.delete(template);
        log.info("Global template deleted with ID: {}", templateId);
    }
    
    @Override
    public GlobalTemplateDTO addItemToTemplate(UUID templateId, String entityType, String jsonData, Integer sortOrder) {
        log.info("Adding item to template ID: {}", templateId);
        GlobalTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Global template not found with ID: " + templateId));
        
        GlobalTemplateItem item = new GlobalTemplateItem();
        item.setTemplate(template);
        item.setEntityType(entityType);
        try {
            JsonNode dataNode = objectMapper.readTree(jsonData);
            item.setData(dataNode);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON data: " + e.getMessage(), e);
        }
        item.setSortOrder(sortOrder != null ? sortOrder : 0);
        
        item = itemRepository.save(item);
        log.info("Item added to template with ID: {}", item.getId());
        
        // Reload template with items for DTO mapping
        template = templateRepository.findByIdWithItems(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Global template not found with ID: " + templateId));
        return templateMapper.toDto(template);
    }
    
    @Override
    public void removeItemFromTemplate(UUID itemId) {
        log.info("Removing item from template with ID: {}", itemId);
        GlobalTemplateItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Template item not found with ID: " + itemId));
        
        itemRepository.delete(item);
        log.info("Template item deleted with ID: {}", itemId);
    }
    
    @Override
    public GlobalTemplateDTO createTemplateFromJson(String jsonData) {
        log.info("Creating template from JSON");
        try {
            // Parse template metadata
            JsonNode root = objectMapper.readTree(jsonData);
            JsonNode templateNode = root.get("template");
            if (templateNode == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JSON must have a 'template' object");
            }
            
            String code = templateNode.get("code").asText();
            
            // Check if template already exists
            if (templateRepository.findByCode(code).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Template with code '" + code + "' already exists");
            }
            
            // Create template
            GlobalTemplate template = new GlobalTemplate();
            template.setName(templateNode.get("name").asText());
            template.setCode(code);
            template.setType(templateNode.get("type").asText());
            if (templateNode.has("version")) {
                template.setVersion(templateNode.get("version").asInt());
            }
            if (templateNode.has("isActive")) {
                template.setIsActive(templateNode.get("isActive").asBoolean());
            }
            
            template = templateRepository.save(template);
            log.info("Created template: {} ({})", template.getName(), template.getCode());
            
            // Parse and create items
            JsonNode itemsNode = root.get("items");
            if (itemsNode != null && itemsNode.isArray()) {
                int sortOrder = 0;
                for (JsonNode itemNode : itemsNode) {
                    GlobalTemplateItem item = new GlobalTemplateItem();
                    item.setTemplate(template);

                    // Handle both "entityType" (camelCase) and "entity_type" (snake_case)
                    JsonNode entityTypeNode = itemNode.get("entityType");
                    if (entityTypeNode == null) {
                        entityTypeNode = itemNode.get("entity_type");
                    }
                    if (entityTypeNode == null || !entityTypeNode.isTextual()) {
                        log.warn("Skipping item without valid entityType in template {}: {}", template.getCode(), itemNode);
                        continue;
                    }
                    // Normalize entity type to Java class name before saving to database
                    String rawEntityType = entityTypeNode.asText();
                    String normalizedEntityType = normalizeEntityType(rawEntityType);
                    if (normalizedEntityType == null) {
                        log.warn("Skipping item with invalid entityType '{}' in template {}: {}", rawEntityType, template.getCode(), itemNode);
                        continue;
                    }

                    // remove sku as it will be auto generated during template copy
                   if ("ProductTemplate".equals(normalizedEntityType)) {
    JsonNode dataNode = itemNode.get("data");
    if (dataNode != null && dataNode.has("sku")) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "PRODUCT_TEMPLATE cannot contain 'sku'. Use 'skuPrefix' only. Found in: " + dataNode.get("name").asText());
    }
} 
                    item.setEntityType(normalizedEntityType);
                    
                    // Store the data field if present, normalize field names to camelCase
                    JsonNode dataNode = itemNode.get("data");
                    if (dataNode != null) {
                        JsonNode normalizedDataNode = normalizeFieldNames(dataNode);
                        item.setData(normalizedDataNode);
                    } else {
                        // If no "data" field, store the entire node except entityType/entity_type
                        JsonNode normalizedItemNode = normalizeFieldNames(itemNode);
                        item.setData(normalizedItemNode);
                    }
                    
                    // Handle both "sortOrder" (camelCase) and "sort_order" (snake_case)
                    int order = sortOrder;
                    if (itemNode.has("sortOrder")) {
                        order = itemNode.get("sortOrder").asInt();
                    } else if (itemNode.has("sort_order")) {
                        order = itemNode.get("sort_order").asInt();
                    }
                    item.setSortOrder(order);
                    
                    // Save item and add to template's collection to maintain bidirectional relationship
                    item = itemRepository.save(item);
                    template.getItems().add(item);
                    sortOrder++;
                }
                log.info("Created {} items for template: {}", template.getItems().size(), template.getCode());
                
                // Save template again to ensure relationship is persisted (even though cascade should handle it)
                template = templateRepository.save(template);
            }
            
            // Reload with items for DTO mapping to ensure fresh state
            template = templateRepository.findByIdWithItems(template.getId())
                    .orElse(template);
            
            log.debug("Template {} has {} items after reload", template.getCode(), template.getItems() != null ? template.getItems().size() : 0);
            return templateMapper.toDto(template);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating template from JSON: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON format: " + e.getMessage(), e);
        }
    }
    
    @Override
    public GlobalTemplateDTO updateTemplateFromJson(UUID templateId, String jsonData) {
        log.info("Updating template {} from JSON", templateId);
        try {
            // First, get the existing template to validate code match
            GlobalTemplate existingTemplate = templateRepository.findById(templateId)
                    .orElseThrow(() -> new ResourceNotFoundException("Global template not found with ID: " + templateId));
            
            // Parse template metadata from JSON
            JsonNode root = objectMapper.readTree(jsonData);
            JsonNode templateNode = root.get("template");
            if (templateNode == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JSON must have a 'template' object");
            }
            
            String jsonCode = templateNode.get("code").asText();
            
            // Validate that the code matches the existing template
            if (!existingTemplate.getCode().equals(jsonCode)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Template code mismatch. You can only update the same template with different items. " +
                    "Expected code: '" + existingTemplate.getCode() + "', but received: '" + jsonCode + "'. " +
                    "To create a new template, use the create endpoint instead.");
            }
            
            // Update template metadata if provided
            if (templateNode.has("name")) {
                existingTemplate.setName(templateNode.get("name").asText());
            }
            if (templateNode.has("type")) {
                existingTemplate.setType(templateNode.get("type").asText());
            }
            if (templateNode.has("version")) {
                existingTemplate.setVersion(templateNode.get("version").asInt());
            }
            if (templateNode.has("isActive")) {
                existingTemplate.setIsActive(templateNode.get("isActive").asBoolean());
            }
            if (templateNode.has("description")) {
                existingTemplate.setDescription(templateNode.get("description").asText());
            }
            
            // Delete all existing items
            int itemCount = existingTemplate.getItems().size();
            itemRepository.deleteAll(existingTemplate.getItems());
            existingTemplate.getItems().clear();
            log.info("Deleted {} existing items for template: {}", itemCount, existingTemplate.getCode());
            
            // Parse and create new items
            JsonNode itemsNode = root.get("items");
            if (itemsNode != null && itemsNode.isArray()) {
                int sortOrder = 0;
                for (JsonNode itemNode : itemsNode) {
                    GlobalTemplateItem item = new GlobalTemplateItem();
                    item.setTemplate(existingTemplate);

                    // Handle both "entityType" (camelCase) and "entity_type" (snake_case)
                    JsonNode entityTypeNode = itemNode.get("entityType");
                    if (entityTypeNode == null) {
                        entityTypeNode = itemNode.get("entity_type");
                    }
                    if (entityTypeNode == null || !entityTypeNode.isTextual()) {
                        log.warn("Skipping item without valid entityType in template {}: {}", existingTemplate.getCode(), itemNode);
                        continue;
                    }
                    // Normalize entity type to Java class name before saving to database
                    String rawEntityType = entityTypeNode.asText();
                    String normalizedEntityType = normalizeEntityType(rawEntityType);
                    if (normalizedEntityType == null) {
                        log.warn("Skipping item with invalid entityType '{}' in template {}: {}", rawEntityType, existingTemplate.getCode(), itemNode);
                        continue;
                    }

                    // Remove sku as it will be auto generated during template copy
                    if ("ProductTemplate".equals(normalizedEntityType)) {
                        JsonNode dataNode = itemNode.get("data");
                        if (dataNode != null && dataNode.has("sku")) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "PRODUCT_TEMPLATE cannot contain 'sku'. Use 'skuPrefix' only. Found in: " + dataNode.get("name").asText());
                        }
                    }
                    item.setEntityType(normalizedEntityType);
                    
                    // Store the data field if present, normalize field names to camelCase
                    JsonNode dataNode = itemNode.get("data");
                    if (dataNode != null) {
                        JsonNode normalizedDataNode = normalizeFieldNames(dataNode);
                        item.setData(normalizedDataNode);
                    } else {
                        // If no "data" field, store the entire node except entityType/entity_type
                        JsonNode normalizedItemNode = normalizeFieldNames(itemNode);
                        item.setData(normalizedItemNode);
                    }
                    
                    // Handle both "sortOrder" (camelCase) and "sort_order" (snake_case)
                    int order = sortOrder;
                    if (itemNode.has("sortOrder")) {
                        order = itemNode.get("sortOrder").asInt();
                    } else if (itemNode.has("sort_order")) {
                        order = itemNode.get("sort_order").asInt();
                    }
                    item.setSortOrder(order);
                    
                    // Save item and add to template's collection to maintain bidirectional relationship
                    item = itemRepository.save(item);
                    existingTemplate.getItems().add(item);
                    sortOrder++;
                }
                log.info("Created {} new items for template: {}", existingTemplate.getItems().size(), existingTemplate.getCode());
            }
            
            // Save template with updated metadata and items
            existingTemplate = templateRepository.save(existingTemplate);
            
            // Reload with items for DTO mapping
            existingTemplate = templateRepository.findByIdWithItems(existingTemplate.getId())
                    .orElse(existingTemplate);
            
            log.debug("Template {} has {} items after update", existingTemplate.getCode(), existingTemplate.getItems() != null ? existingTemplate.getItems().size() : 0);
            return templateMapper.toDto(existingTemplate);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating template from JSON: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON format: " + e.getMessage(), e);
        }
    }
    
    /**
     * Normalizes entity type names to match Java class names.
     * Handles variations like: UOM, UNIT_OF_MEASURE -> UnitOfMeasure
     */
    private String normalizeEntityType(String entityType) {
        if (entityType == null || entityType.trim().isEmpty()) {
            return null;
        }
        String normalized = entityType.trim();
        String upper = normalized.toUpperCase();
        
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
        
        if (normalized.length() > 0) {
            return normalized.substring(0, 1).toUpperCase() + normalized.substring(1);
        }
        return normalized;
    }
    
    /**
     * Normalizes field names from snake_case to camelCase in a JSON node.
     */
    private static final Map<String, String> FIELD_MAP = Map.ofEntries(
    Map.entry("unitOfMeasure_code", "uomCode"),
    Map.entry("sku_prefix", "skuPrefix"),
    Map.entry("requires_expiry", "requiresExpiry"),
    Map.entry("reorder_point", "reorderPoint"),
    Map.entry("country_code", "countryCode"),
    Map.entry("tax_rate", "taxRate"),
    Map.entry("contact_person", "contactPerson"),
    Map.entry("is_active", "isActive"),
    Map.entry("parent_code", "parentCode"),
    Map.entry("category_code", "categoryCode"),
    Map.entry("brand_name", "brandName")
);

private JsonNode normalizeFieldNames(JsonNode node) {
    if (node == null || node.isNull()) return node;

    if (node.isObject()) {
        ObjectNode normalized = objectMapper.createObjectNode();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();

            // Skip metadata
            if (key.equals("entityType") || key.equals("entity_type") ||
                key.equals("sortOrder") || key.equals("sort_order")) {
                continue;
            }

            String camelKey = FIELD_MAP.getOrDefault(key, snakeToCamel(key));
            normalized.set(camelKey, normalizeFieldNames(entry.getValue()));
        }
        return normalized;
    } else if (node.isArray()) {
        ArrayNode array = objectMapper.createArrayNode();
        node.forEach(n -> array.add(normalizeFieldNames(n)));
        return array;
    }
    return node;
}
    
    private String snakeToCamel(String snake) {
        if (snake == null || snake.isEmpty()) {
            return snake;
        }
        StringBuilder camel = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : snake.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    camel.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    camel.append(c);
                }
            }
        }
        return camel.toString();
    }
}

