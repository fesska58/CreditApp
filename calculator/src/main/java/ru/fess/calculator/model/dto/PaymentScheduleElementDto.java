package ru.fess.calculator.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PaymentScheduleElementDto {
    @Schema(description = "First name", example = "Alex")
    private Integer number;

    @Schema(description = "Payment number", example = "1")
    private LocalDate date;

    @Schema(description = "Total payment", example = "55113.53")
    private BigDecimal totalPayment;

    @Schema(description = "Interest part of payment", example = "1366.74")
    private BigDecimal interestPayment;

    @Schema(description = "Principal part of payment", example = "17800.00")
    private BigDecimal debtPayment;

    @Schema(description = "Remaining debt after payment", example = "380122.10")
    private BigDecimal remainingDebt;
}
