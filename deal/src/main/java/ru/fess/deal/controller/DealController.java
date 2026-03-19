package ru.fess.deal.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.fess.deal.dto.LoanOfferDto;
import ru.fess.deal.dto.LoanStatementRequestDto;
import ru.fess.deal.service.DealServiceImpl;

import java.util.List;
import java.util.UUID;

@Tag(name = "Deal", description = "API for calculate & select offer")
@RestController
@RequestMapping("/deal")
@RequiredArgsConstructor
public class DealController {
    private final DealServiceImpl dealService;

    @PostMapping("/statement")
    public List<LoanOfferDto> calculateOffers(@RequestBody LoanStatementRequestDto request){
        return dealService.calculateOffers(request);
    }

    @PostMapping("/offer/select/{statementId}")
    public ResponseEntity<Void> selectOffers(
            @PathVariable UUID statementId,
            @RequestBody LoanOfferDto request){

        dealService.selectOffer(statementId, request);
        return ResponseEntity.ok().build();
    }
}
