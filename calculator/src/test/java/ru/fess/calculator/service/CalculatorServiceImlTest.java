package ru.fess.calculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.fess.calculator.config.CalculatorProperties;
import ru.fess.calculator.model.dto.CreditDto;
import ru.fess.calculator.model.dto.LoanOfferDto;
import ru.fess.calculator.model.dto.LoanStatementRequestDto;
import ru.fess.calculator.model.dto.ScoringDataDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalculatorServiceImlTest {
    private CalculatorService calculatorService;

    @BeforeEach
    void setUp() {
        CalculatorProperties properties = new CalculatorProperties();
        properties.setBase(BigDecimal.valueOf(20));
        properties.setInsuranceDiscount(BigDecimal.valueOf(3));
        properties.setSalaryDiscount(BigDecimal.valueOf(1));

        calculatorService = new CalculatorServiceIml(properties);
    }

    @Test
    void calculateOffers_shouldReturnFourOffers() {
        LoanStatementRequestDto loanStatementRequestDto = new LoanStatementRequestDto();
        loanStatementRequestDto.setAmount(BigDecimal.valueOf(500000));
        loanStatementRequestDto.setTerm(24);
        loanStatementRequestDto.setFirstName("Alex");
        loanStatementRequestDto.setLastName("Petrov");
        loanStatementRequestDto.setMiddleName("Jovanovich");
        loanStatementRequestDto.setEmail("test@test.com");
        loanStatementRequestDto.setBirthDate(LocalDate.of(2000,5,1));
        loanStatementRequestDto.setPassportSeries("1234");
        loanStatementRequestDto.setPassportNumber("123456");

        List<LoanOfferDto> loanOffers = calculatorService.calculateOffers(loanStatementRequestDto);

        assertEquals(4, loanOffers.size());
    }

    @Test
    void calculateOffers_shouldReturnCorrectPaymentSchedule() {
        ScoringDataDto request = new ScoringDataDto();
        request.setAmount(BigDecimal.valueOf(500000));
        request.setTerm(24);
        request.setFirstName("Alex");
        request.setLastName("Petrov");
        request.setMiddleName("Jovanovich");
        request.setBirthdate(LocalDate.of(2000,5,1));
        request.setPassportSeries("1234");
        request.setPassportNumber("123456");
        request.setIsInsuranceEnabled(true);
        request.setIsSalaryClient(true);

        CreditDto credit = calculatorService.calculateCredit(request);

        assertEquals(24, credit.getPaymentSchedule().size());
    }

    @Test
    void calculateCredit_monthlyPaymentShouldBePositive() {
        ScoringDataDto request = new ScoringDataDto();
        request.setAmount(BigDecimal.valueOf(500000));
        request.setTerm(24);
        request.setFirstName("Alex");
        request.setLastName("Petrov");
        request.setMiddleName("Jovanovich");
        request.setBirthdate(LocalDate.of(2000,5,1));
        request.setPassportSeries("1234");
        request.setPassportNumber("123456");
        request.setIsInsuranceEnabled(true);
        request.setIsSalaryClient(true);

        CreditDto result = calculatorService.calculateCredit(request);

        assertTrue(result.getMonthlyPayment().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateOffers_shouldApplyInsuranceDiscount() {
        LoanStatementRequestDto request = new LoanStatementRequestDto();
        request.setAmount(BigDecimal.valueOf(500000));
        request.setTerm(24);
        request.setFirstName("Alex");
        request.setLastName("Petrov");
        request.setMiddleName("Jovanovich");
        request.setEmail("test@test.com");
        request.setBirthDate(LocalDate.of(2000,5,1));
        request.setPassportSeries("1234");
        request.setPassportNumber("123456");

        List<LoanOfferDto> loanOffers = calculatorService.calculateOffers(request);

        assertFalse(loanOffers.isEmpty());

        LoanOfferDto offerWithInsurance = loanOffers.stream()
                .filter(LoanOfferDto::getIsInsuranceEnabled)
                .findFirst()
                .orElse(null);

        assertNotNull(offerWithInsurance);
    }

}
