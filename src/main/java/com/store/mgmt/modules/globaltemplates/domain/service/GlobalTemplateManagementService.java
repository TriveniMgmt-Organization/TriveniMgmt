package com.store.mgmt.modules.globaltemplates.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.store.mgmt.shared.domain.exception.ResourceNotFoundException;
import com.store.mgmt.modules.globaltemplates.domain.model.GlobalTemplate;
import com.store.mgmt.modules.globaltemplates.domain.model.GlobalTemplateItem;
import com.store.mgmt.modules.globaltemplates.domain.repository.GlobalTemplateItemRepository;
import com.store.mgmt.modules.globaltemplates.domain.repository.GlobalTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Domain service for managing global templates.
 * Handles complex operations like creating/updating templates from JSON.
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class GlobalTemplateManagementService {

    private final GlobalTemplateRepository templateRepository;
    private final GlobalTemplateItemRepository itemRepository;
    private final ObjectMapper objectMapper;

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

    public GlobalTemplate createTemplateFromJson(String jsonData) {
        log.info("Creating template from JSON");
        try {
            JsonNode root = objectMapper.readTree(jsonData);
            JsonNode templateNode = root.get("template");
            if (templateNode == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JSON must have a 'template' object");
            }

            String code = templateNode.get("code").asText();

            if (templateRepository.findByCode(code).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Template with code '" + code + "' already exists");
            }

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
            if (templateNode.has("description")) {
                template.setDescription(templateNode.get("description").asText());
            }

            template = templateRepository.save(template);
            log.info("Created template: {} ({})", template.getName(), template.getCode());

            processItems(template, root.get("items"));

            return templateRepository.findByIdWithItems(template.getId()).orElse(template);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating template from JSON: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON format: " + e.getMessage(), e);
        }
    }

    public GlobalTemplate updateTemplateFromJson(UUID templateId, String jsonData) {
        log.info("Updating template {} from JSON", templateId);
        try {
            GlobalTemplate existingTemplate = templateRepository.findById(templateId)
                    .orElseThrow(() -> new ResourceNotFoundException("Global template not found with ID: " + templateId));

            JsonNode root = objectMapper.readTree(jsonData);
            JsonNode templateNode = root.get("template");
            if (templateNode == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JSON must have a 'template' object");
            }

            String jsonCode = templateNode.get("code").asText();

            if (!existingTemplate.getCode().equals(jsonCode)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Template code mismatch. Expected: '" + existingTemplate.getCode() + "', received: '" + jsonCode + "'");
            }

            // Update metadata
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

            // Delete existing items
            itemRepository.deleteAll(existingTemplate.getItems());
            existingTemplate.getItems().clear();

            // Process new items
            processItems(existingTemplate, root.get("items"));

            existingTemplate = templateRepository.save(existingTemplate);
            return templateRepository.findByIdWithItems(existingTemplate.getId()).orElse(existingTemplate);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating template from JSON: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON format: " + e.getMessage(), e);
        }
    }

    public GlobalTemplate addItemToTemplate(UUID templateId, String entityType, String jsonData, Integer sortOrder) {
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

        itemRepository.save(item);
        log.info("Item added to template");

        return templateRepository.findByIdWithItems(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Global template not found with ID: " + templateId));
    }

    public void removeItemFromTemplate(UUID itemId) {
        log.info("Removing item with ID: {}", itemId);
        GlobalTemplateItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Template item not found with ID: " + itemId));

        itemRepository.delete(item);
        log.info("Template item deleted");
    }

    private void processItems(GlobalTemplate template, JsonNode itemsNode) {
        if (itemsNode == null || !itemsNode.isArray()) {
            return;
        }

        int sortOrder = 0;
        for (JsonNode itemNode : itemsNode) {
            GlobalTemplateItem item = new GlobalTemplateItem();
            item.setTemplate(template);

            JsonNode entityTypeNode = itemNode.get("entityType");
            if (entityTypeNode == null) {
                entityTypeNode = itemNode.get("entity_type");
            }
            if (entityTypeNode == null || !entityTypeNode.isTextual()) {
                log.warn("Skipping item without valid entityType");
                continue;
            }

            String rawEntityType = entityTypeNode.asText();
            String normalizedEntityType = normalizeEntityType(rawEntityType);
            if (normalizedEntityType == null) {
                log.warn("Skipping item with invalid entityType '{}'", rawEntityType);
                continue;
            }

            if ("ProductTemplate".equals(normalizedEntityType)) {
                JsonNode dataNode = itemNode.get("data");
                if (dataNode != null && dataNode.has("sku")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "PRODUCT_TEMPLATE cannot contain 'sku'. Use 'skuPrefix' only.");
                }
            }
            item.setEntityType(normalizedEntityType);

            JsonNode dataNode = itemNode.get("data");
            if (dataNode != null) {
                item.setData(normalizeFieldNames(dataNode));
            } else {
                item.setData(normalizeFieldNames(itemNode));
            }

            int order = sortOrder;
            if (itemNode.has("sortOrder")) {
                order = itemNode.get("sortOrder").asInt();
            } else if (itemNode.has("sort_order")) {
                order = itemNode.get("sort_order").asInt();
            }
            item.setSortOrder(order);

            item = itemRepository.save(item);
            template.getItems().add(item);
            sortOrder++;
        }
        log.info("Created {} items for template: {}", template.getItems().size(), template.getCode());
    }

    private String normalizeEntityType(String entityType) {
        if (entityType == null || entityType.trim().isEmpty()) {
            return null;
        }
        String upper = entityType.trim().toUpperCase();

        return switch (upper) {
            case "UOM", "UNIT_OF_MEASURE", "UNITOFMEASURE" -> "UnitOfMeasure";
            case "PRODUCT_TEMPLATE", "PRODUCTTEMPLATE" -> "ProductTemplate";
            case "TAX_RULE", "TAXRULE" -> "TaxRule";
            case "BRAND" -> "Brand";
            case "CATEGORY" -> "Category";
            case "LOCATION", "INVENTORYLOCATION", "INVENTORY_LOCATION" -> "InventoryLocation";
            case "SUPPLIER" -> "Supplier";
            default -> entityType.substring(0, 1).toUpperCase() + entityType.substring(1);
        };
    }

    private JsonNode normalizeFieldNames(JsonNode node) {
        if (node == null || node.isNull()) return node;

        if (node.isObject()) {
            ObjectNode normalized = objectMapper.createObjectNode();
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();

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
