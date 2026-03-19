package ru.fess.deal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.fess.deal.client.CalculatorClient;
import ru.fess.deal.dto.LoanOfferDto;
import ru.fess.deal.dto.LoanStatementRequestDto;
import ru.fess.deal.entity.Statement;
import ru.fess.deal.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DealServiceImplTest {
    @Mock
    private CalculatorClient client;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private StatementRepository statementRepository;
    @Mock
    private PassportRepository passportRepository;
    @Mock
    private CreditRepository creditRepository;
    @Mock
    private StatusHistoryRepository statusHistoryRepository;
    @InjectMocks
    private DealServiceImpl dealService;

    @Test
    void calculateOffers_shouldReturnOffersWithStatementId(){
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

        List<LoanOfferDto> offers = List.of(
                LoanOfferDto.builder().build(),
                LoanOfferDto.builder().build()
        );

        when(client.getLoanOffer(any())).thenReturn(offers);

        Statement savedStatement = Statement.builder()
                .id(UUID.randomUUID())
                .build();

        when(statementRepository.save(any())).thenReturn(savedStatement);

        List<LoanOfferDto> result = dealService.calculateOffers(loanStatementRequestDto);

        assertEquals(2, result.size());
        result.forEach(o -> assertNotNull(o.getStatementId()));

        verify(passportRepository).save(any());
        verify(clientRepository).save(any());
        verify(statementRepository).save(any());
        verify(statusHistoryRepository).save(any());
        verify(client).getLoanOffer(any());
    }

}
