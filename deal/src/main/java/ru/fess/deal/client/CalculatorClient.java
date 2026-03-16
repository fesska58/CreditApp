package ru.fess.deal.client;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.fess.deal.dto.LoanOfferDto;
import ru.fess.deal.dto.LoanStatementRequestDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculatorClient {
    private final RestClient restClient;

    public List<LoanOfferDto> getLoanOffer(LoanStatementRequestDto request){
        return restClient.post()
                .uri("calculator/offers")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<List<LoanOfferDto>>() {});
    }
}
