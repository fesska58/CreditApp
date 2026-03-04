package ru.fess.calculator.service;

import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.stereotype.Service;
import ru.fess.calculator.model.dto.LoanOfferDto;
import ru.fess.calculator.model.dto.LoanStatementRequestDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class CalculatorServiceIml implements CalculatorService {
    private static final BigDecimal BASE_RATE = BigDecimal.valueOf(20);
    private static final BigDecimal INSURANCE_DISCOUNT = BigDecimal.valueOf(3);
    private static final BigDecimal SALARY_DISCOUNT = BigDecimal.valueOf(1);

    @Override
    public List<LoanOfferDto> calculateOffers(LoanStatementRequestDto requestDto) {
        return List.of(
                buildOffer(requestDto, false, false),
                buildOffer(requestDto, true, false),
                buildOffer(requestDto, false, true),
                buildOffer(requestDto, true, true)
        );
    }

    private LoanOfferDto buildOffer(LoanStatementRequestDto requestDto,
                                    boolean isInsuranceDiscount,
                                    boolean isSalaryDiscount) {
        BigDecimal rate = calculateRate(isInsuranceDiscount, isSalaryDiscount);

        BigDecimal monthlyPayment = calculateMonthlyPayments(requestDto.getAmount(), requestDto.getTerm(), rate);

        BigDecimal totalAmount = monthlyPayment
                .multiply(BigDecimal.valueOf(requestDto.getTerm()))
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        return LoanOfferDto.builder()
                .statementId(UUID.randomUUID())
                .requestedAmount(requestDto.getAmount())
                .totalAmount(totalAmount)
                .term(requestDto.getTerm())
                .monthlyPayment(monthlyPayment)
                .rate(rate)
                .isInsuranceEnabled(isInsuranceDiscount)
                .isSalaryClient(isSalaryDiscount)
                .build();
    }

    private BigDecimal calculateRate(boolean insurance, boolean salary) {
        BigDecimal rate = BASE_RATE;

        if (insurance) {
            rate = rate.subtract(INSURANCE_DISCOUNT);
        }

        if (salary) {
            rate = rate.subtract(SALARY_DISCOUNT);
        }

        return rate;
    }

    private BigDecimal calculateMonthlyPayments(BigDecimal amount,
                                                Integer term,
                                                BigDecimal rate) {
        BigDecimal monthlyRate = rate
                .divide(BigDecimal.valueOf(100), 10, BigDecimal.ROUND_HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, BigDecimal.ROUND_HALF_UP);

        BigDecimal annuityCoefficient = monthlyRate.multiply(
                BigDecimal.ONE.add(monthlyRate).pow(term)
                        .divide(
                                BigDecimal.ONE.add(monthlyRate).pow(term).subtract(BigDecimal.ONE),
                                10, BigDecimal.ROUND_HALF_UP
                        )
        );

        return amount
                .multiply(annuityCoefficient)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
