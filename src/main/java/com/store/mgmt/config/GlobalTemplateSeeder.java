package com.store.mgmt.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.mgmt.modules.globaltemplates.domain.model.GlobalTemplate;
import com.store.mgmt.modules.globaltemplates.domain.model.GlobalTemplateItem;
import com.store.mgmt.modules.globaltemplates.domain.repository.GlobalTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Seeds global templates from JSON files in resources/seeds/globaltemplates.
 *
 * Global templates are platform-wide reference data that organizations can apply
 * to quickly set up their stores with industry-standard categories, products, etc.
 */
@Component
public class GlobalTemplateSeeder {

    private static final Logger logger = LoggerFactory.getLogger(GlobalTemplateSeeder.class);
    private static final String TEMPLATES_PATH = "classpath:seeds/globaltemplates/*.json";

    private final GlobalTemplateRepository globalTemplateRepository;
    private final ObjectMapper objectMapper;

    public GlobalTemplateSeeder(
            GlobalTemplateRepository globalTemplateRepository,
            ObjectMapper objectMapper
    ) {
        this.globalTemplateRepository = globalTemplateRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Seeds global templates from JSON files.
     * Skips templates that already exist (by code).
     *
     * @return number of templates seeded
     */
    @Transactional
    public int seedGlobalTemplates() {
        logger.info("GlobalTemplateSeeder: Seeding global templates from JSON files...");

        int seededCount = 0;

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(TEMPLATES_PATH);

            if (resources.length == 0) {
                logger.warn("GlobalTemplateSeeder: No template JSON files found at {}", TEMPLATES_PATH);
                return 0;
            }

            logger.info("GlobalTemplateSeeder: Found {} template JSON files.", resources.length);

            for (Resource resource : resources) {
                try {
                    String filename = resource.getFilename();
                    if (filename == null || !filename.endsWith(".json")) {
                        continue;
                    }

                    boolean seeded = seedTemplateFromResource(resource);
                    if (seeded) {
                        seededCount++;
                    }
                } catch (Exception e) {
                    logger.error("GlobalTemplateSeeder: Failed to seed template from {}: {}",
                            resource.getFilename(), e.getMessage(), e);
                }
            }

            logger.info("GlobalTemplateSeeder: Seeded {} new global templates.", seededCount);
        } catch (IOException e) {
            logger.error("GlobalTemplateSeeder: Failed to read template files: {}", e.getMessage(), e);
        }

        return seededCount;
    }

    private boolean seedTemplateFromResource(Resource resource) throws IOException {
        String filename = resource.getFilename();
        logger.debug("GlobalTemplateSeeder: Processing template file: {}", filename);

        try (InputStream is = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(is);

            JsonNode templateNode = root.get("template");
            if (templateNode == null) {
                logger.warn("GlobalTemplateSeeder: No 'template' node in {}, skipping.", filename);
                return false;
            }

            String code = getTextValue(templateNode, "code");
            if (code == null || code.isBlank()) {
                logger.warn("GlobalTemplateSeeder: No 'code' in template from {}, skipping.", filename);
                return false;
            }

            // Check if template already exists
            Optional<GlobalTemplate> existing = globalTemplateRepository.findByCode(code);
            if (existing.isPresent()) {
                logger.debug("GlobalTemplateSeeder: Template {} already exists, skipping.", code);
                return false;
            }

            // Create the template
            GlobalTemplate template = new GlobalTemplate();
            template.setCode(code);
            template.setName(getTextValue(templateNode, "name"));
            template.setType(getTextValue(templateNode, "type"));
            template.setDescription(getTextValue(templateNode, "description"));
            template.setIsActive(getBooleanValue(templateNode, "isActive", true));
            template.setVersion(getIntValue(templateNode, "version", 1));
            template.setCreatedAt(LocalDateTime.now());
            template.setCreatedBy("global-template-seeder");

            // Parse and add items
            JsonNode itemsNode = root.get("items");
            if (itemsNode != null && itemsNode.isArray()) {
                Set<GlobalTemplateItem> items = new LinkedHashSet<>();
                int sortOrder = 0;

                for (JsonNode itemNode : itemsNode) {
                    GlobalTemplateItem item = createTemplateItem(template, itemNode, sortOrder++);
                    if (item != null) {
                        items.add(item);
                    }
                }

                template.setItems(items);
            }

            globalTemplateRepository.save(template);
            logger.info("GlobalTemplateSeeder: Seeded template: {} ({})", template.getName(), code);
            return true;
        }
    }

    private GlobalTemplateItem createTemplateItem(GlobalTemplate template, JsonNode itemNode, int sortOrder) {
        String entityType = getTextValue(itemNode, "entity_type");
        if (entityType == null) {
            entityType = getTextValue(itemNode, "entityType");
        }

        if (entityType == null || entityType.isBlank()) {
            logger.warn("GlobalTemplateSeeder: Item missing entity_type, skipping.");
            return null;
        }

        JsonNode dataNode = itemNode.get("data");
        if (dataNode == null) {
            logger.warn("GlobalTemplateSeeder: Item missing data, skipping.");
            return null;
        }

        GlobalTemplateItem item = new GlobalTemplateItem();
        item.setTemplate(template);
        item.setEntityType(normalizeEntityType(entityType));
        item.setData(dataNode);
        item.setSortOrder(sortOrder);
        item.setCreatedAt(LocalDateTime.now());
        item.setCreatedBy("global-template-seeder");

        return item;
    }

    /**
     * Normalizes entity type names to standard format.
     * Supports various naming conventions (SCREAMING_SNAKE, camelCase, PascalCase).
     */
    private String normalizeEntityType(String entityType) {
        if (entityType == null) {
            return null;
        }

        String upper = entityType.toUpperCase().replace("-", "_");

        // Map common variations to standard names
        return switch (upper) {
            case "UOM", "UNIT_OF_MEASURE", "UNITOFMEASURE" -> "UOM";
            case "PRODUCT_TEMPLATE", "PRODUCTTEMPLATE" -> "PRODUCT_TEMPLATE";
            case "TAX_RULE", "TAXRULE" -> "TAX_RULE";
            case "DAMAGE_LOSS_REASON", "DAMAGELOSSREASON" -> "DAMAGE_LOSS_REASON";
            default -> upper;
        };
    }

    private String getTextValue(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return (fieldNode != null && fieldNode.isTextual()) ? fieldNode.asText() : null;
    }

    private boolean getBooleanValue(JsonNode node, String field, boolean defaultValue) {
        JsonNode fieldNode = node.get(field);
        return (fieldNode != null && fieldNode.isBoolean()) ? fieldNode.asBoolean() : defaultValue;
    }

    private int getIntValue(JsonNode node, String field, int defaultValue) {
        JsonNode fieldNode = node.get(field);
        return (fieldNode != null && fieldNode.isNumber()) ? fieldNode.asInt() : defaultValue;
    }
}
