package ru.fess.deal.service;

import ru.fess.deal.dto.LoanOfferDto;
import ru.fess.deal.dto.LoanStatementRequestDto;

import java.util.List;
import java.util.UUID;

public interface DealService {
    List<LoanOfferDto> calculateOffers(LoanStatementRequestDto request);
    void selectOffer(UUID statementId, LoanOfferDto offer);
}
