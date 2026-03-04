package ru.fess.calculator.service;

import ru.fess.calculator.model.dto.LoanOfferDto;
import ru.fess.calculator.model.dto.LoanStatementRequestDto;

import java.util.List;

public interface CalculatorService {
    List<LoanOfferDto> calculateOffers(LoanStatementRequestDto requestDto);
}
