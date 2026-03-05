package ru.fess.calculator.controllers;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.fess.calculator.model.dto.CreditDto;
import ru.fess.calculator.model.dto.LoanOfferDto;
import ru.fess.calculator.model.dto.LoanStatementRequestDto;
import ru.fess.calculator.model.dto.ScoringDataDto;
import ru.fess.calculator.service.CalculatorServiceIml;

import java.util.List;

@RestController
@RequestMapping("/calculator")

public class CalculatorController {
    private final CalculatorServiceIml calculatorService;

    public CalculatorController(CalculatorServiceIml calculatorService) {
        this.calculatorService = calculatorService;
    }

    @PostMapping("/offers")
    public List<LoanOfferDto> calculateOffers(@Valid @RequestBody LoanStatementRequestDto requestDto){
        return calculatorService.calculateOffers(requestDto);
    }

    @PostMapping("/calc")
    public CreditDto calculateCredit(@Valid @RequestBody ScoringDataDto requestDto){
        return calculatorService.calculateCredit(requestDto);
    }
}
