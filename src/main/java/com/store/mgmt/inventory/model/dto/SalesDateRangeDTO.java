package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Date range filter for sales")
@Data
public class SalesDateRangeDTO {

        @Schema(description = "Start date (yyyy-MM-dd)", example = "2024-01-01", required = true)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDateTime startDate;

        @Schema(description = "End date (yyyy-MM-dd)", example = "2024-01-31", required = true)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDateTime endDate;

    }
