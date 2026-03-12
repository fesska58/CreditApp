package ru.fess.calculator.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.fess.calculator.config.CalculatorProperties;
import ru.fess.calculator.model.dto.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculatorServiceImpl implements CalculatorService {
    private final CalculatorProperties calculatorProperties;

    @Override
    public List<LoanOfferDto> calculateOffers(LoanStatementRequestDto requestDto) {
        log.info("Start calculate offers: {}", requestDto);

        List<LoanOfferDto> offers = new ArrayList<>();

        offers.add(buildOffer(requestDto, false, false));
        offers.add(buildOffer(requestDto, true, false));
        offers.add(buildOffer(requestDto, false, true));
        offers.add(buildOffer(requestDto, true, true));

        log.info("End calculate offers: {}", offers);

        return offers;
    }

    @Override
    public CreditDto calculateCredit(ScoringDataDto requestDto) {
        log.info("Start calculate credit: {}", requestDto);

        BigDecimal rate = calculateRate(requestDto.getIsInsuranceEnabled(), requestDto.getIsSalaryClient());

        BigDecimal monthlyPayment = calculateMonthlyPayments(
                requestDto.getAmount(),
                requestDto.getTerm(),
                rate);

        BigDecimal totalAmount = monthlyPayment
                .multiply(
                    BigDecimal.valueOf(requestDto.getTerm()))
                .setScale(2, RoundingMode.HALF_UP);

        List<PaymentScheduleElementDto> schedule =
                calculatePaymentSchedule(
                        requestDto.getAmount(),
                        requestDto.getTerm(),
                        rate,
                        monthlyPayment
                );

        BigDecimal psk = calculatePsk(
                totalAmount,
                requestDto.getAmount()
        );

        log.debug("rate={}, totalAmount={}, monthlyPayment={}, schedule={}, psk={}",
                rate, totalAmount,monthlyPayment, schedule,psk);

        CreditDto result = CreditDto.builder()
                .amount(requestDto.getAmount())
                .term(requestDto.getTerm())
                .monthlyPayment(monthlyPayment)
                .rate(rate)
                .psk(psk)
                .isInsuranceEnabled(requestDto.getIsInsuranceEnabled())
                .isSalaryClient(requestDto.getIsSalaryClient())
                .paymentSchedule(schedule)
                .build();

        log.info("End calculate credit: {}", result);

        return result;
    }



    private LoanOfferDto buildOffer(LoanStatementRequestDto requestDto,
                                    boolean isInsuranceDiscount,
                                    boolean isSalaryDiscount) {
        log.info("Start build offer: {}", requestDto);

        BigDecimal rate = calculateRate(isInsuranceDiscount, isSalaryDiscount);

        BigDecimal monthlyPayment = calculateMonthlyPayments(requestDto.getAmount(), requestDto.getTerm(), rate);

        BigDecimal totalAmount = monthlyPayment
                .multiply(BigDecimal.valueOf(requestDto.getTerm()))
                .setScale(2, RoundingMode.HALF_UP);

        log.info("rate={}, totalAmount={}, monthlyPayment={}",
                rate, totalAmount,monthlyPayment);

        LoanOfferDto result = LoanOfferDto.builder()
                .statementId(UUID.randomUUID())
                .requestedAmount(requestDto.getAmount())
                .totalAmount(totalAmount)
                .term(requestDto.getTerm())
                .monthlyPayment(monthlyPayment)
                .rate(rate)
                .isInsuranceEnabled(isInsuranceDiscount)
                .isSalaryClient(isSalaryDiscount)
                .build();
        log.info("End build offer: result={}", result);

        return result;
    }

    private BigDecimal calculateRate(boolean insurance, boolean salary) {
        BigDecimal rate = calculatorProperties.getBase();
        log.debug("Start calculate rate: {}", rate);
        if (insurance) {
            rate = rate.subtract(calculatorProperties.getInsuranceDiscount());
            log.debug("New rate: {}", rate);
        }

        if (salary) {
            rate = rate.subtract(calculatorProperties.getSalaryDiscount());
            log.debug("New rate: {}", rate);
        }

        log.info("End calculate rate: {}", rate);
        return rate;
    }

    private BigDecimal calculateMonthlyPayments(BigDecimal amount,
                                                Integer term,
                                                BigDecimal rate) {
        log.info("Start calculate monthly payments: amount={}, term={} rate={}", amount, term,rate);
        BigDecimal monthlyRate = rate
                .divide(BigDecimal.valueOf(100), 10,RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal annuityCoefficient = monthlyRate.multiply(
                BigDecimal.ONE.add(monthlyRate).pow(term)
                        .divide(
                                BigDecimal.ONE.add(monthlyRate).pow(term).subtract(BigDecimal.ONE),
                                10, RoundingMode.HALF_UP
                        )
        );

        log.debug("monthlyRate={}, annuityCoefficient={}", monthlyRate, annuityCoefficient);

        BigDecimal result = amount
                .multiply(annuityCoefficient)
                .setScale(2, RoundingMode.HALF_UP);

        log.info("End calculate monthly payments: result={}", result);

        return result;
    }

    private BigDecimal calculatePsk(
            BigDecimal totalAmount,
            BigDecimal amount
    ){
        log.info("Start calculate psk: totalAmount={}, amount={}", totalAmount, amount);

        BigDecimal overpayment  = totalAmount.subtract(amount);
        log.debug("overpayment={}", overpayment);

        BigDecimal result = overpayment
                .divide(amount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        log.info("End calculate psk: result={}", result);

        return result;
    }

    private List<PaymentScheduleElementDto> calculatePaymentSchedule(
             BigDecimal amount,
             Integer term,
             BigDecimal rate,
             BigDecimal monthlyPayment
    ){
        log.info("amount={}, term={}, rate={}, monthlyPayment={}", amount, term, rate, monthlyPayment);

        List<PaymentScheduleElementDto> schedule = new ArrayList<>();

        BigDecimal monthlyRate = rate
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal remainingDebt = amount;

        LocalDate paymentDate = LocalDate.now().plusMonths(1);

        log.debug("monthlyRate={}, remainingDebt={}", monthlyRate, remainingDebt);

        for (int i = 1; i <= term; i++) {

            BigDecimal interestPayment = remainingDebt
                    .multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal debtPayment = monthlyPayment
                    .subtract(interestPayment)
                    .setScale(2, RoundingMode.HALF_UP);

            remainingDebt = remainingDebt
                    .subtract(debtPayment)
                    .setScale(2, RoundingMode.HALF_UP);

            schedule.add(
                    PaymentScheduleElementDto.builder()
                            .number(i)
                            .date(paymentDate)
                            .totalPayment(monthlyPayment)
                            .interestPayment(interestPayment)
                            .debtPayment(debtPayment)
                            .remainingDebt(
                                    remainingDebt.compareTo(BigDecimal.ZERO) < 0
                                            ? BigDecimal.ZERO
                                            : remainingDebt
                            )
                            .build()
            );

            paymentDate = paymentDate.plusMonths(1);
        }

        log.info("schedule={}", schedule);
        return schedule;
    }

}
