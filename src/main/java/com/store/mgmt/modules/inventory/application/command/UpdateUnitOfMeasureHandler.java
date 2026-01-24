package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.UnitOfMeasure;
import com.store.mgmt.modules.inventory.domain.repository.UnitOfMeasureRepository;
import com.store.mgmt.modules.inventory.application.dto.UnitOfMeasureResponseDTO;
import com.store.mgmt.modules.inventory.application.service.UnitOfMeasureMapper;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for UpdateUnitOfMeasureCommand.
 */
@Component
@Transactional
public class UpdateUnitOfMeasureHandler implements CommandHandler<UpdateUnitOfMeasureCommand, UnitOfMeasureResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(UpdateUnitOfMeasureHandler.class);

    private final UnitOfMeasureRepository uomRepository;
    private final UnitOfMeasureMapper uomMapper;

    public UpdateUnitOfMeasureHandler(UnitOfMeasureRepository uomRepository, UnitOfMeasureMapper uomMapper) {
        this.uomRepository = uomRepository;
        this.uomMapper = uomMapper;
    }

    @Override
    public UnitOfMeasureResponseDTO handle(UpdateUnitOfMeasureCommand cmd) {
        log.debug("Updating unit of measure: {}", cmd.id());

        UnitOfMeasure uom = uomRepository.findByIdAndOrganizationId(cmd.id(), cmd.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Unit of measure not found with ID: " + cmd.id()));

        // Check for duplicate name if name is being changed
        if (cmd.name() != null && !cmd.name().equals(uom.getName())) {
            uomRepository.findByNameAndOrganizationId(cmd.name(), cmd.organizationId()).ifPresent(existing -> {
                if (!existing.getId().equals(cmd.id())) {
                    throw new IllegalArgumentException("Unit of measure with name '" + cmd.name() + "' already exists in this organization");
                }
            });
            uom.setName(cmd.name());
        }

        // Check for duplicate code if code is being changed
        if (cmd.code() != null && !cmd.code().equals(uom.getCode())) {
            uomRepository.findByCodeAndOrganizationId(cmd.code(), cmd.organizationId()).ifPresent(existing -> {
                if (!existing.getId().equals(cmd.id())) {
                    throw new IllegalArgumentException("Unit of measure with code '" + cmd.code() + "' already exists in this organization");
                }
            });
            uom.setCode(cmd.code());
        }

        UnitOfMeasure saved = uomRepository.save(uom);
        log.info("Updated unit of measure with ID: {}", saved.getId());

        return uomMapper.toResponseDTO(saved);
    }
}
