package ru.fess.deal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.fess.deal.dto.LoanOfferDto;
import ru.fess.deal.dto.LoanStatementRequestDto;
import ru.fess.deal.service.DealService;

import java.util.List;

@RestController
@RequestMapping("/deal")
@RequiredArgsConstructor
public class DealController {
    private final DealService dealService;

    @PostMapping("/statement")
    public List<LoanOfferDto> calculateOffers(@RequestBody LoanStatementRequestDto request){
        return dealService.calculateOffers(request);
    }

    @PostMapping("/offer/select")
    public void selectOffers(@RequestBody LoanOfferDto request){
        dealService.selectOffer(request);
    }
}
