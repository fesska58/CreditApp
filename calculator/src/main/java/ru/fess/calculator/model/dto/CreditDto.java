package ru.fess.calculator.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CreditDto {
    @Schema(description = "Loan amount", example = "500000")
    private BigDecimal amount;

    @Schema(description = "Loan term", example = "12")
    private Integer term;

    @Schema(description = "MonthlyPayment", example = "500000")
    private BigDecimal monthlyPayment;

    @Schema(description = "Monthly payment", example = "24481.56")
    private BigDecimal rate;

    @Schema(description = "PSK", example = "17.5")
    private BigDecimal psk;

    @Schema(description = "Insurance enabled", example = "true")
    private Boolean isInsuranceEnabled;

    @Schema(description = "Salary client", example = "true")
    private Boolean isSalaryClient;

    @Schema(description = "Payment schedule")
    private List<PaymentScheduleElementDto> paymentSchedule;
}
