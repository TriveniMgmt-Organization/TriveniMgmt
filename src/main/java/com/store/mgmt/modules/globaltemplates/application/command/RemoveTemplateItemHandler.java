package com.store.mgmt.modules.globaltemplates.application.command;

import com.store.mgmt.modules.globaltemplates.domain.service.GlobalTemplateManagementService;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class RemoveTemplateItemHandler implements CommandHandler<RemoveTemplateItemCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(RemoveTemplateItemHandler.class);

    private final GlobalTemplateManagementService templateManagementService;

    public RemoveTemplateItemHandler(GlobalTemplateManagementService templateManagementService) {
        this.templateManagementService = templateManagementService;
    }

    @Override
    public Void handle(RemoveTemplateItemCommand cmd) {
        log.info("Removing template item: {}", cmd.itemId());

        templateManagementService.removeItemFromTemplate(cmd.itemId());

        log.info("Removed template item: {}", cmd.itemId());
        return null;
    }
}
