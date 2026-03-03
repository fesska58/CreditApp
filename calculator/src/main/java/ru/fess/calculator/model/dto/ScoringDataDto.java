package ru.fess.calculator.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScoringDataDto {
    private BigDecimal amount;

    private Integer term;

    private String firstName;

    private String lastName;

    private String middleName;

    private String gender;

    private LocalDate birthdate;

    private String passportSeries;

    private String passportNumber;

    private Boolean isInsuranceEnabled;

    private Boolean isSalaryClient;
}
