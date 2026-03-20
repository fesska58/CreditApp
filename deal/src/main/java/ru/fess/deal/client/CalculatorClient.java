package ru.fess.deal.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.fess.deal.dto.CreditDto;
import ru.fess.deal.dto.LoanOfferDto;
import ru.fess.deal.dto.LoanStatementRequestDto;
import ru.fess.deal.dto.ScoringDataDto;

import java.util.List;

@Service
public class CalculatorClient {
    private final RestClient restClient;

    @Autowired
    public CalculatorClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<LoanOfferDto> getLoanOffer(LoanStatementRequestDto request){
        return restClient.post()
                .uri("calculator/offers")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<List<LoanOfferDto>>() {});
    }

    public CreditDto calculateCredit(ScoringDataDto scoringDataDto){
        return restClient.post()
                .uri("calculator/calc")
                .body(scoringDataDto)
                .retrieve()
                .body(CreditDto.class);
    }
}
