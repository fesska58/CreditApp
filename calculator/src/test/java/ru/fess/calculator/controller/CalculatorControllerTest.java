package ru.fess.calculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.fess.calculator.controllers.CalculatorController;
import ru.fess.calculator.model.dto.LoanOfferDto;
import ru.fess.calculator.model.dto.LoanStatementRequestDto;
import ru.fess.calculator.service.CalculatorService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@ExtendWith(MockitoExtension.class)
class CalculatorControllerTest {
    private final CalculatorService calculatorService =
            Mockito.mock(CalculatorService.class);

    private final CalculatorController controller =
            new CalculatorController(calculatorService);

    private final MockMvc mockMvc =
            MockMvcBuilders.standaloneSetup(controller).build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void calculateOffers_shouldReturnOffers() throws Exception {

        LoanStatementRequestDto loanStatementRequestDto = new LoanStatementRequestDto();
        loanStatementRequestDto.setAmount(BigDecimal.valueOf(500000));
        loanStatementRequestDto.setTerm(24);
        loanStatementRequestDto.setFirstName("Alex");
        loanStatementRequestDto.setLastName("Petrov");
        loanStatementRequestDto.setMiddleName("Jovanovich");
        loanStatementRequestDto.setEmail("test@test.com");
        loanStatementRequestDto.setBirthDate(LocalDate.of(2000,5,1));
        loanStatementRequestDto.setPassportSeries("1234");
        loanStatementRequestDto.setPassportNumber("123456");

        LoanOfferDto offer = new LoanOfferDto();
        offer.setStatementId(UUID.randomUUID());
        offer.setTotalAmount(BigDecimal.valueOf(10000));
        offer.setRequestedAmount(BigDecimal.valueOf(500000));
        offer.setTerm(24);
        offer.setMonthlyPayment(BigDecimal.valueOf(25447.90));
        offer.setRate(BigDecimal.valueOf(20));
        offer.setIsInsuranceEnabled(true);
        offer.setIsSalaryClient(true);

        Mockito.when(calculatorService.calculateOffers(Mockito.any()))
                .thenReturn(List.of(offer));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanStatementRequestDto)))
                .andExpect(status().isOk());
    }
}
