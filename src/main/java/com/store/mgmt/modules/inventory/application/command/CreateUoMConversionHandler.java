package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.UnitOfMeasure;
import com.store.mgmt.modules.inventory.domain.model.UoMConversion;
import com.store.mgmt.modules.inventory.domain.repository.UnitOfMeasureRepository;
import com.store.mgmt.modules.inventory.domain.repository.UoMConversionRepository;
import com.store.mgmt.modules.inventory.application.dto.UoMConversionResponseDTO;
import com.store.mgmt.modules.inventory.application.service.UoMConversionMapper;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for CreateUoMConversionCommand.
 */
@Component
@Transactional
public class CreateUoMConversionHandler implements CommandHandler<CreateUoMConversionCommand, UoMConversionResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreateUoMConversionHandler.class);

    private final UoMConversionRepository conversionRepository;
    private final UnitOfMeasureRepository uomRepository;
    private final UoMConversionMapper mapper;

    public CreateUoMConversionHandler(
            UoMConversionRepository conversionRepository,
            UnitOfMeasureRepository uomRepository,
            UoMConversionMapper mapper
    ) {
        this.conversionRepository = conversionRepository;
        this.uomRepository = uomRepository;
        this.mapper = mapper;
    }

    @Override
    public UoMConversionResponseDTO handle(CreateUoMConversionCommand cmd) {
        log.debug("Creating UoM conversion from {} to {} with ratio {}",
                cmd.fromUomId(), cmd.toUomId(), cmd.ratio());

        // Validate that from and to UoMs are different
        if (cmd.fromUomId().equals(cmd.toUomId())) {
            throw new IllegalArgumentException("From and To UoM must be different");
        }

        // Validate that from UoM exists
        UnitOfMeasure fromUom = uomRepository.findById(cmd.fromUomId())
                .orElseThrow(() -> new EntityNotFoundException("From UoM not found with ID: " + cmd.fromUomId()));

        // Validate that to UoM exists
        UnitOfMeasure toUom = uomRepository.findById(cmd.toUomId())
                .orElseThrow(() -> new EntityNotFoundException("To UoM not found with ID: " + cmd.toUomId()));

        // Check for duplicate conversion
        conversionRepository.findByFromUomIdAndToUomId(cmd.fromUomId(), cmd.toUomId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Conversion from " + fromUom.getName() + " to " + toUom.getName() + " already exists"
                    );
                });

        UoMConversion conversion = new UoMConversion();
        conversion.setFromUom(fromUom);
        conversion.setToUom(toUom);
        conversion.setRatio(cmd.ratio());

        UoMConversion saved = conversionRepository.save(conversion);
        log.info("Created UoM conversion with ID: {} (1 {} = {} {})",
                saved.getId(), fromUom.getName(), cmd.ratio(), toUom.getName());

        return mapper.toResponseDTO(saved);
    }
}
