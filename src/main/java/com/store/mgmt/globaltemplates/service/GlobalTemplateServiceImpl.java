package com.store.mgmt.globaltemplates.service;

import com.store.mgmt.common.exception.ResourceNotFoundException;
import com.store.mgmt.globaltemplates.mapper.GlobalTemplateMapper;
import com.store.mgmt.globaltemplates.model.dto.*;
import com.store.mgmt.globaltemplates.model.entity.GlobalTemplate;
import com.store.mgmt.globaltemplates.model.entity.GlobalTemplateItem;
import com.store.mgmt.globaltemplates.repository.GlobalTemplateItemRepository;
import com.store.mgmt.globaltemplates.repository.GlobalTemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class GlobalTemplateServiceImpl implements GlobalTemplateService {
    
    private final GlobalTemplateRepository templateRepository;
    private final GlobalTemplateItemRepository itemRepository;
    private final GlobalTemplateMapper templateMapper;
    
    public GlobalTemplateServiceImpl(
            GlobalTemplateRepository templateRepository,
            GlobalTemplateItemRepository itemRepository,
            GlobalTemplateMapper templateMapper) {
        this.templateRepository = templateRepository;
        this.itemRepository = itemRepository;
        this.templateMapper = templateMapper;
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
        
        return templateMapper.toDto(template);
    }
    
    @Override
    @Transactional(readOnly = true)
    public GlobalTemplateDTO getTemplateById(UUID templateId) {
        log.debug("Fetching global template with ID: {}", templateId);
        GlobalTemplate template = templateRepository.findByIdWithItems(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Global template not found with ID: " + templateId));
        
        // Items are already loaded via JOIN FETCH in repository query
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
        List<GlobalTemplate> templates = templateRepository.findAll();
        return templateMapper.toDtoList(templates);
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
        item.setData(jsonData);
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
}

