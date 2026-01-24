package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.BatchLot;
import com.store.mgmt.modules.inventory.domain.model.Supplier;
import com.store.mgmt.modules.inventory.domain.repository.BatchLotRepository;
import com.store.mgmt.modules.inventory.domain.repository.SupplierRepository;
import com.store.mgmt.modules.inventory.application.dto.BatchLotResponseDTO;
import com.store.mgmt.modules.inventory.application.service.BatchLotMapper;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for CreateBatchLotCommand.
 */
@Component
@Transactional
public class CreateBatchLotHandler implements CommandHandler<CreateBatchLotCommand, BatchLotResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreateBatchLotHandler.class);

    private final BatchLotRepository batchLotRepository;
    private final SupplierRepository supplierRepository;
    private final BatchLotMapper mapper;

    public CreateBatchLotHandler(
            BatchLotRepository batchLotRepository,
            SupplierRepository supplierRepository,
            BatchLotMapper mapper
    ) {
        this.batchLotRepository = batchLotRepository;
        this.supplierRepository = supplierRepository;
        this.mapper = mapper;
    }

    @Override
    public BatchLotResponseDTO handle(CreateBatchLotCommand cmd) {
        log.debug("Creating batch/lot: {}", cmd.batchNumber());

        // Check for duplicate batch number
        batchLotRepository.findByBatchNumber(cmd.batchNumber()).ifPresent(existing -> {
            throw new IllegalArgumentException("Batch/lot with number '" + cmd.batchNumber() + "' already exists");
        });

        BatchLot batchLot = new BatchLot();
        batchLot.setBatchNumber(cmd.batchNumber());
        batchLot.setManufactureDate(cmd.manufactureDate());
        batchLot.setExpiryDate(cmd.expiryDate());
        batchLot.setActive(true);

        // Set supplier if provided
        if (cmd.supplierId() != null) {
            Supplier supplier = supplierRepository.findById(cmd.supplierId())
                    .orElseThrow(() -> new EntityNotFoundException("Supplier not found with ID: " + cmd.supplierId()));
            batchLot.setSupplier(supplier);
        }

        BatchLot saved = batchLotRepository.save(batchLot);
        log.info("Created batch/lot with ID: {}, batchNumber: {}", saved.getId(), saved.getBatchNumber());

        return mapper.toResponseDTO(saved);
    }
}
