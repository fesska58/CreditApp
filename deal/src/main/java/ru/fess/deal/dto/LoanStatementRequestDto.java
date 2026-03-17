package ru.fess.deal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanStatementRequestDto {
    @Schema(description = "Loan amount", example = "500000")
    @NotNull
    private BigDecimal amount;

    @Schema(description = "Term in months", example = "12")
    @NotNull
    private Integer term;

    @Schema(description = "First name", example = "Alex")
    @NotBlank
    @Size(min = 2, max = 30)
    private String firstName;

    @Schema(description = "Last name", example = "Petrov")
    @NotBlank
    @Size(min = 2, max = 30)
    private String lastName;

    @Schema(description = "Middle name", example = "Alex")
    @NotBlank
    @Size(min = 2, max = 30)
    private String middleName;

    @Schema(description = "Email", example = "alex@mail.com")
    @Email
    @NotBlank
    private String email;

    @Schema(description = "Birthdate", example = "2000-10-01")
    private LocalDate birthDate;

    @Schema(description = "Passport series", example = "1234")
    @NotBlank
    @Pattern(regexp = "\\d{4}")
    private String passportSeries;

    @Schema(description = "Passport numbers", example = "987654")
    @NotBlank
    @Pattern(regexp = "\\d{6}")
    private String passportNumber;

}