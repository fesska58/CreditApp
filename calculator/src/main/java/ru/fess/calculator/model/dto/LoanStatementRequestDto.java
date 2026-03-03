package ru.fess.calculator.model.dto;


import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanStatementRequestDto {
    @NotNull
    private BigDecimal amount;

    @NotNull
    private Integer term;

    @NotBlank
    @Size(min = 2, max = 30)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 30)
    private String lastName;

    @NotBlank
    @Size(min = 2, max = 30)
    private String middleName;

    @Email
    @NotBlank
    private String email;

    private LocalDate birthDate;
    @NotBlank
    @Pattern(regexp = "\\d{4}")
    private String passportSeries;

    @NotBlank
    @Pattern(regexp = "\\d{6}")
    private String passportNumber;

}
