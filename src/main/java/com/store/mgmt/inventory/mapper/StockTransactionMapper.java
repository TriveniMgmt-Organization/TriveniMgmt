package com.store.mgmt.inventory.mapper;

import com.store.mgmt.inventory.model.dto.CreateStockTransactionDTO;
import com.store.mgmt.inventory.model.dto.StockTransactionDTO;
import com.store.mgmt.inventory.model.entity.StockTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StockTransactionMapper {

    @Mapping(source = "inventoryItem.id", target = "inventoryItemId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "type", target = "type", qualifiedByName = "transactionTypeToString")
    StockTransactionDTO toDto(StockTransaction transaction);
    List<StockTransactionDTO> toDtoList(List<StockTransaction> transactions);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "inventoryItem", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "type", expression = "java(com.store.mgmt.inventory.model.enums.TransactionType.valueOf(dto.getType()))")
    StockTransaction toEntity(CreateStockTransactionDTO dto);

    // Helper method for type conversion: TransactionType enum -> String
    @org.mapstruct.Named("transactionTypeToString")
    default String transactionTypeToString(com.store.mgmt.inventory.model.enums.TransactionType type) {
        return type != null ? type.name() : null;
    }
}

