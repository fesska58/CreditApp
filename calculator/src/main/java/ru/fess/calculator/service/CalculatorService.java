package ru.fess.calculator.service;

import ru.fess.calculator.model.dto.CreditDto;
import ru.fess.calculator.model.dto.LoanOfferDto;
import ru.fess.calculator.model.dto.LoanStatementRequestDto;
import ru.fess.calculator.model.dto.ScoringDataDto;

import java.util.List;

public interface CalculatorService {
    List<LoanOfferDto> calculateOffers(LoanStatementRequestDto requestDto);
    CreditDto calculateCredit(ScoringDataDto requestDto);
}
