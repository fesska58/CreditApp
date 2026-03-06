package ru.fess.calculator.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanOfferDto {
    @Schema(description = "Application ID", example = "uuid-value")
    private UUID statementId;

    @Schema(description = "Requested amount", example = "500000")
    private BigDecimal requestedAmount;

    @Schema(description = "Total amount", example = "51039.10")
    private BigDecimal totalAmount;

    @Schema(description = "Loan term", example = "6")
    private Integer term;

    @Schema(description = "Monthly payment", example = "25447.90")
    private BigDecimal monthlyPayment;

    @Schema(description = "Interest rate", example = "20")
    private BigDecimal rate;

    @Schema(description = "Insurance enabled", example = "false")
    private Boolean isInsuranceEnabled;

    @Schema(description = "Salary client", example = "false")
    private Boolean isSalaryClient;
}
