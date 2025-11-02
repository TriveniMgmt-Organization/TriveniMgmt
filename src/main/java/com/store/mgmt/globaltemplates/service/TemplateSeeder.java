package com.store.mgmt.globaltemplates.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.mgmt.globaltemplates.model.entity.GlobalTemplate;
import com.store.mgmt.globaltemplates.model.entity.GlobalTemplateItem;
import com.store.mgmt.globaltemplates.repository.GlobalTemplateRepository;
import com.store.mgmt.globaltemplates.repository.GlobalTemplateItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class TemplateSeeder {
    
    private final GlobalTemplateRepository templateRepository;
    private final GlobalTemplateItemRepository itemRepository;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    
    @Value("${app.templates.seed-path:seeds/globaltemplates}")
    private String seedPath;
    
    public TemplateSeeder(
            GlobalTemplateRepository templateRepository,
            GlobalTemplateItemRepository itemRepository,
            ObjectMapper objectMapper,
            ResourceLoader resourceLoader) {
        this.templateRepository = templateRepository;
        this.itemRepository = itemRepository;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        // Only seed if templates table is empty
        if (templateRepository.count() > 0) {
            log.info("Global templates already exist ({} found). Skipping template seeding.", templateRepository.count());
            return;
        }
        
        log.info("Starting global template seeding from path: {}", seedPath);
        
        try {
            Path seedsDir = Paths.get(seedPath);
            int totalFilesProcessed = 0;
            
            // If directory doesn't exist as filesystem path, try resource path
            if (!Files.exists(seedsDir)) {
                Resource resource = resourceLoader.getResource("classpath:" + seedPath);
                if (resource.exists()) {
                    try {
                        URI uri = resource.getURI();
                        
                        // Handle both filesystem and JAR paths
                        if ("jar".equals(uri.getScheme())) {
                            // Running from JAR file
                            FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                            seedsDir = fileSystem.getPath("/" + seedPath);
                            
                            // Process files
                            try (Stream<Path> paths = Files.walk(seedsDir)) {
                                List<Path> jsonFiles = paths.filter(Files::isRegularFile)
                                     .filter(path -> path.toString().endsWith(".json"))
                                     .collect(java.util.stream.Collectors.toList());
                                log.info("Found {} JSON template file(s) to process", jsonFiles.size());
                                jsonFiles.forEach(path -> processSeedFileFromJar(path, resourceLoader, seedPath));
                                totalFilesProcessed = jsonFiles.size();
                            }
                            fileSystem.close();
                        } else {
                            // Running from filesystem (development)
                            seedsDir = Paths.get(uri);
                            try (Stream<Path> paths = Files.walk(seedsDir)) {
                                List<Path> jsonFiles = paths.filter(Files::isRegularFile)
                                     .filter(path -> path.toString().endsWith(".json"))
                                     .collect(java.util.stream.Collectors.toList());
                                log.info("Found {} JSON template file(s) to process", jsonFiles.size());
                                jsonFiles.forEach(this::processSeedFile);
                                totalFilesProcessed = jsonFiles.size();
                            }
                        }
                    } catch (IOException e) {
                        log.error("Error accessing resource path {}: {}", seedPath, e.getMessage(), e);
                        return;
                    }
                } else {
                    log.warn("Template seed directory not found: {}. Skipping template seeding.", seedPath);
                    return;
                }
            } else {
                // Find all JSON files in the filesystem directory
                try (Stream<Path> paths = Files.walk(seedsDir)) {
                    List<Path> jsonFiles = paths.filter(Files::isRegularFile)
                         .filter(path -> path.toString().endsWith(".json"))
                         .collect(java.util.stream.Collectors.toList());
                    log.info("Found {} JSON template file(s) to process", jsonFiles.size());
                    jsonFiles.forEach(this::processSeedFile);
                    totalFilesProcessed = jsonFiles.size();
                }
            }
            
            log.info("Global template seeding completed. Processed {} template file(s).", totalFilesProcessed);
        } catch (IOException e) {
            log.error("Error reading seed directory: {}", e.getMessage(), e);
        }
    }
    
    private void processSeedFileFromJar(Path pathInJar, ResourceLoader resourceLoader, String seedPath) {
        try {
            // Extract filename from path
            String fileName = pathInJar.getFileName().toString();
            String resourcePath = seedPath + "/" + fileName;
            
            Resource resource = resourceLoader.getResource("classpath:" + resourcePath);
            try (InputStream inputStream = resource.getInputStream()) {
                String content = new String(inputStream.readAllBytes());
                processSeedFileContent(fileName, content);
            }
        } catch (IOException e) {
            log.error("Error reading seed file from JAR {}: {}", pathInJar, e.getMessage(), e);
        }
    }
    
    private void processSeedFile(Path jsonFile) {
        try {
            log.info("Processing seed file: {}", jsonFile.getFileName());
            String content = Files.readString(jsonFile);
            processSeedFileContent(jsonFile.getFileName().toString(), content);
        } catch (IOException e) {
            log.error("Error processing seed file {}: {}", jsonFile.getFileName(), e.getMessage(), e);
        }
    }
    
    private void processSeedFileContent(String fileName, String content) {
        try {
            
            // Parse template metadata
            JsonNode root = objectMapper.readTree(content);
            JsonNode templateNode = root.get("template");
            if (templateNode == null) {
                log.warn("Template metadata not found in file: {}", fileName);
                return;
            }
            
            String code = templateNode.get("code").asText();
            
            // Check if template already exists
            if (templateRepository.findByCode(code).isPresent()) {
                log.debug("Template with code '{}' already exists, skipping", code);
                return;
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
                    item.setEntityType(normalizedEntityType);
                    if (!rawEntityType.equals(normalizedEntityType)) {
                        log.debug("Normalized entityType '{}' to '{}' in template {}", rawEntityType, normalizedEntityType, template.getCode());
                    }
                    
                    // Store the data field if present, normalize field names to camelCase
                    JsonNode dataNode = itemNode.get("data");
                    if (dataNode != null) {
                        // Normalize field names from snake_case to camelCase before storing
                        JsonNode normalizedDataNode = normalizeFieldNames(dataNode);
                        item.setData(normalizedDataNode);
                    } else {
                        // If no "data" field, store the entire node except entityType/entity_type
                        // Normalize field names in the entire node
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
                    
                    itemRepository.save(item);
                    sortOrder++;
                }
                log.info("Created {} items for template: {}", itemsNode.size(), template.getCode());
            }
            
        } catch (Exception e) {
            log.error("Error processing seed file {}: {}", fileName, e.getMessage(), e);
        }
    }
    
    /**
     * Normalizes entity type names to match Java class names.
     * This ensures consistent entity type names are stored in the database,
     * regardless of how they appear in JSON seed files.
     * 
     * Handles variations like:
     * - "UOM", "UNIT_OF_MEASURE", "UnitOfMeasure" -> "UnitOfMeasure"
     * - "PRODUCT_TEMPLATE", "PRODUCTTEMPLATE", "ProductTemplate" -> "ProductTemplate"
     * - "TAX_RULE", "TAXRULE", "TaxRule" -> "TaxRule"
     * - "BRAND", "Brand" -> "Brand"
     * - "CATEGORY", "Category" -> "Category"
     * 
     * @param entityType the raw entity type from JSON
     * @return normalized entity type matching Java class name, or null if invalid
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
        
        // If it's already in proper camelCase/PascalCase, capitalize first letter
        // Handle camelCase: "unitOfMeasure" -> "UnitOfMeasure"
        // Handle PascalCase: "Brand" -> "Brand"
        if (normalized.length() > 0) {
            String firstChar = normalized.substring(0, 1);
            String rest = normalized.length() > 1 ? normalized.substring(1) : "";
            
            // Check if already capitalized
            if (firstChar.equals(firstChar.toUpperCase())) {
                // Already in PascalCase, return as-is
                return normalized;
            } else {
                // Convert first letter to uppercase
                return firstChar.toUpperCase() + rest;
            }
        }
        
        return normalized;
    }
    
    /**
     * Normalizes field names in JSON from snake_case to camelCase.
     * This ensures consistent field names are stored in the database,
     * regardless of how they appear in JSON seed files.
     * 
     * Examples:
     * - "country_code" -> "countryCode"
     * - "tax_rate" -> "taxRate"
     * - "category_code" -> "categoryCode"
     * - "unit_of_measure_code" -> "unitOfMeasureCode"
     * - "cost_price" -> "costPrice"
     * - "retail_price" -> "retailPrice"
     * 
     * @param jsonNode the JSON node to normalize
     * @return new JSON node with normalized field names
     */
    private JsonNode normalizeFieldNames(JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isNull()) {
            return jsonNode;
        }
        
        if (jsonNode.isObject()) {
            com.fasterxml.jackson.databind.node.ObjectNode normalizedObject = objectMapper.createObjectNode();
            
            jsonNode.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                
                // Convert snake_case to camelCase
                String normalizedKey = snakeToCamel(key);
                
                // Recursively normalize nested objects
                if (value.isObject() || value.isArray()) {
                    normalizedObject.set(normalizedKey, normalizeFieldNames(value));
                } else {
                    normalizedObject.set(normalizedKey, value);
                }
            });
            
            return normalizedObject;
        } else if (jsonNode.isArray()) {
            com.fasterxml.jackson.databind.node.ArrayNode normalizedArray = objectMapper.createArrayNode();
            
            for (JsonNode element : jsonNode) {
                normalizedArray.add(normalizeFieldNames(element));
            }
            
            return normalizedArray;
        }
        
        // For primitives (string, number, boolean), return as-is
        return jsonNode;
    }
    
    /**
     * Converts snake_case to camelCase.
     * 
     * @param snakeCase the snake_case string
     * @return camelCase string
     */
    private String snakeToCamel(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }
        
        // Check if it's already camelCase (no underscores or first char is lowercase)
        if (!snakeCase.contains("_")) {
            return snakeCase;
        }
        
        StringBuilder camelCase = new StringBuilder();
        boolean nextUpperCase = false;
        
        for (int i = 0; i < snakeCase.length(); i++) {
            char c = snakeCase.charAt(i);
            
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    camelCase.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    camelCase.append(c);
                }
            }
        }
        
        return camelCase.toString();
    }
}

