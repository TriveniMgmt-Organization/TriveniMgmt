package com.store.mgmt.globaltemplates.service;

import com.store.mgmt.globaltemplates.model.dto.*;

import java.util.List;
import java.util.UUID;

public interface GlobalTemplateService {
    
    GlobalTemplateDTO createTemplate(CreateGlobalTemplateDTO createDTO);
    
    GlobalTemplateDTO getTemplateById(UUID templateId);
    
    GlobalTemplateDTO getTemplateByCode(String code);
    
    List<GlobalTemplateDTO> getAllTemplates();
    
    List<GlobalTemplateDTO> getActiveTemplates();
    
    List<GlobalTemplateDTO> getTemplatesByType(String type);
    
    GlobalTemplateDTO updateTemplate(UUID templateId, UpdateGlobalTemplateDTO updateDTO);
    
    void deleteTemplate(UUID templateId);
    
    GlobalTemplateDTO addItemToTemplate(UUID templateId, String entityType, String jsonData, Integer sortOrder);
    
    void removeItemFromTemplate(UUID itemId);
}

