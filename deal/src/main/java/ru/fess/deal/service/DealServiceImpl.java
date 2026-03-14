package ru.fess.deal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.fess.deal.client.CalculatorClient;
import ru.fess.deal.dto.LoanOfferDto;
import ru.fess.deal.dto.LoanStatementRequestDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService{
    private final CalculatorClient client;
    @Override
    public List<LoanOfferDto> calculateOffers(LoanStatementRequestDto request) {
        return client.getLoanOffer(request);
    }
}
