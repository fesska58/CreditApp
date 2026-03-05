package ru.fess.calculator.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@Tag(name = "Calculator", description = "API for loan calculation")
@RestController
@RequestMapping("/calculator")
@RequiredArgsConstructor
public class CalculatorController {
    private final CalculatorServiceIml calculatorService;

    @Operation(summary = "Calculate loan offers")
    @PostMapping("/offers")
    public List<LoanOfferDto> calculateOffers(@Valid @RequestBody LoanStatementRequestDto requestDto){
        return calculatorService.calculateOffers(requestDto);
    }

    @Operation(summary = "Calculate credit")
    @PostMapping("/calc")
    public CreditDto calculateCredit(@Valid @RequestBody ScoringDataDto requestDto){
        return calculatorService.calculateCredit(requestDto);
    }
}
