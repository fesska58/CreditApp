package ru.fess.deal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoringDataDto {
    @Schema(description = "Loan amount", example = "500000")
    private BigDecimal amount;

    @Schema(description = "Term in months", example = "24")
    private Integer term;

    @Schema(description = "First name", example = "Alex")
    private String firstName;

    @Schema(description = "First name", example = "Petrov")
    private String lastName;

    @Schema(description = "First name", example = "Alex")
    private String middleName;

    @Schema(description = "Gender", example = "MALE")
    private String gender;

    @Schema(description = "Birthdate", example = "1990-01-01")
    private LocalDate birthdate;

    @Schema(description = "Passport series", example = "1234")
    private String passportSeries;

    @Schema(description = "Passport number", example = "987654")
    private String passportNumber;

    @Schema(description = "Insurance enabled", example = "true")
    private Boolean isInsuranceEnabled;

    @Schema(description = "Salary client", example = "true")
    private Boolean isSalaryClient;
}
