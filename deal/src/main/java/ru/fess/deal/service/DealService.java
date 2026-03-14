package ru.fess.deal.service;

import ru.fess.deal.dto.LoanOfferDto;
import ru.fess.deal.dto.LoanStatementRequestDto;

import java.util.List;

public interface DealService {
    List<LoanOfferDto> calculateOffers(LoanStatementRequestDto request);
}
