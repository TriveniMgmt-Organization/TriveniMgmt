package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.UnitOfMeasure;
import com.store.mgmt.modules.inventory.domain.repository.UnitOfMeasureRepository;
import com.store.mgmt.modules.inventory.application.dto.UnitOfMeasureResponseDTO;
import com.store.mgmt.modules.inventory.application.service.UnitOfMeasureMapper;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for CreateUnitOfMeasureCommand.
 */
@Component
@Transactional
public class CreateUnitOfMeasureHandler implements CommandHandler<CreateUnitOfMeasureCommand, UnitOfMeasureResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreateUnitOfMeasureHandler.class);

    private final UnitOfMeasureRepository uomRepository;
    private final UnitOfMeasureMapper uomMapper;

    public CreateUnitOfMeasureHandler(
            UnitOfMeasureRepository uomRepository,
            UnitOfMeasureMapper uomMapper
    ) {
        this.uomRepository = uomRepository;
        this.uomMapper = uomMapper;
    }

    @Override
    public UnitOfMeasureResponseDTO handle(CreateUnitOfMeasureCommand cmd) {
        log.debug("Creating unit of measure: {} for organization: {}", cmd.name(), cmd.organizationId());

        // Check for duplicate name within organization
        uomRepository.findByNameAndOrganizationId(cmd.name(), cmd.organizationId()).ifPresent(existing -> {
            throw new IllegalArgumentException("Unit of measure with name '" + cmd.name() + "' already exists in this organization");
        });

        // Check for duplicate code within organization
        uomRepository.findByCodeAndOrganizationId(cmd.code(), cmd.organizationId()).ifPresent(existing -> {
            throw new IllegalArgumentException("Unit of measure with code '" + cmd.code() + "' already exists in this organization");
        });

        UnitOfMeasure uom = new UnitOfMeasure();
        uom.setOrganizationId(cmd.organizationId());
        uom.setName(cmd.name());
        uom.setCode(cmd.code());

        UnitOfMeasure saved = uomRepository.save(uom);
        log.info("Created unit of measure with ID: {}", saved.getId());

        return uomMapper.toResponseDTO(saved);
    }
}
