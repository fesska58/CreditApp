package ru.fess.deal.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.fess.deal.dto.LoanOfferDto;
import ru.fess.deal.dto.LoanStatementRequestDto;

import java.util.List;

@FeignClient(name = "calculator", url = "http://localhost:8080")
public interface CalculatorClient {
    @PostMapping("/calculator/offers")
    List<LoanOfferDto> getLoanOffer(@RequestBody LoanStatementRequestDto request);
}
