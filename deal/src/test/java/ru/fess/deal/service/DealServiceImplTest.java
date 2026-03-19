package ru.fess.deal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.fess.deal.client.CalculatorClient;
import ru.fess.deal.dto.CreditDto;
import ru.fess.deal.dto.LoanOfferDto;
import ru.fess.deal.dto.LoanStatementRequestDto;
import ru.fess.deal.dto.ScoringDataDto;
import ru.fess.deal.entity.*;
import ru.fess.deal.enums.ApplicationStatus;
import ru.fess.deal.enums.ChangeType;
import ru.fess.deal.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

        UUID savedStatementId = UUID.randomUUID();

        List<LoanOfferDto> offers = List.of(
                LoanOfferDto.builder().statementId(savedStatementId).build(),
                LoanOfferDto.builder().statementId(savedStatementId).build()
        );

        when(client.getLoanOffer(any())).thenReturn(offers);
        when(statementRepository.save(any())).thenReturn(
                Statement.builder().id(savedStatementId).build()
        );

        List<LoanOfferDto> result = dealService.calculateOffers(loanStatementRequestDto);

        assertEquals(2, result.size());
        result.forEach(offer ->
                assertNotNull(offer.getStatementId(), "statementId должен быть установлен")
        );

        verify(passportRepository).save(any());
        verify(clientRepository).save(any());
        verify(statementRepository).save(any());
        verify(statusHistoryRepository).save(any());
        verify(client).getLoanOffer(any());
    }

    @Test
    void selectOffer_shouldUpdateStatementAndCreateCredit() {
        // === Arrange ===
        UUID statementId = UUID.randomUUID();

        // Создаём зависимые сущности
        Passport passport = Passport.builder()
                .series("1234")
                .number("567890")
                .build();

        Client clientEntity = Client.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .birthDate(LocalDate.of(1990, 1, 1))
                .passport(passport)
                .build();

        Statement statement = Statement.builder()
                .id(statementId)
                .client(clientEntity)
                .status(ApplicationStatus.PREAPPROVAL)
                .build();

        LoanOfferDto offer = LoanOfferDto.builder()
                .statementId(statementId)
                .requestedAmount(BigDecimal.valueOf(300_000))
                .term(24)
                .isInsuranceEnabled(true)
                .isSalaryClient(false)
                .build();

        CreditDto creditDto = CreditDto.builder()
                .amount(BigDecimal.valueOf(300_000))
                .term(24)
                .monthlyPayment(BigDecimal.valueOf(15_000))
                .rate(BigDecimal.valueOf(15.5))
                .psk(BigDecimal.valueOf(16.2))
                .isInsuranceEnabled(true)
                .isSalaryClient(false)
                .paymentSchedule(Collections.emptyList())
                .build();

        when(statementRepository.findById(statementId))
                .thenReturn(Optional.of(statement));

        when(statementRepository.save(any(Statement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // возвращаем то, что передали

        when(client.calculateCredit(any(ScoringDataDto.class)))
                .thenReturn(creditDto);

        when(creditRepository.save(any(Credit.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(statusHistoryRepository.save(any(StatusHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        dealService.selectOffer(statementId, offer);

        verify(statementRepository).findById(statementId);


        verify(statusHistoryRepository, times(2)).save(any(StatusHistory.class));


        verify(creditRepository).save(any(Credit.class));

        ArgumentCaptor<ScoringDataDto> scoringCaptor = ArgumentCaptor.forClass(ScoringDataDto.class);
        verify(client).calculateCredit(scoringCaptor.capture());
        ScoringDataDto capturedScoring = scoringCaptor.getValue();

        assertEquals(BigDecimal.valueOf(300_000), capturedScoring.getAmount());
        assertEquals(24, capturedScoring.getTerm());
        assertEquals("Ivan", capturedScoring.getFirstName());
        assertEquals("Ivanov", capturedScoring.getLastName());
        assertEquals("Ivanovich", capturedScoring.getMiddleName());
        assertEquals(LocalDate.of(1990, 1, 1), capturedScoring.getBirthdate());
        assertEquals("1234", capturedScoring.getPassportSeries());
        assertEquals("567890", capturedScoring.getPassportNumber());
        assertTrue(capturedScoring.getIsInsuranceEnabled());
        assertFalse(capturedScoring.getIsSalaryClient());


        ArgumentCaptor<Statement> statementCaptor = ArgumentCaptor.forClass(Statement.class);
        verify(statementRepository, atLeastOnce()).save(statementCaptor.capture());

        Statement savedStatement = statementCaptor.getValue();
        assertEquals(ApplicationStatus.CC_APPROVED, savedStatement.getStatus());
        assertEquals(offer, savedStatement.getAppliedOffer());
        assertNotNull(savedStatement.getCredit());


        Credit savedCredit = savedStatement.getCredit();
        assertEquals(BigDecimal.valueOf(300_000), savedCredit.getAmount());
        assertEquals(24, savedCredit.getTerm());
        assertEquals(BigDecimal.valueOf(15_000), savedCredit.getMonthlyPayment());
        assertEquals(BigDecimal.valueOf(15.5), savedCredit.getRate());
        assertTrue(savedCredit.isInsuranceEnabled());
        assertFalse(savedCredit.isSalaryClient());


        ArgumentCaptor<StatusHistory> historyCaptor = ArgumentCaptor.forClass(StatusHistory.class);
        verify(statusHistoryRepository, times(2)).save(historyCaptor.capture());
        List<StatusHistory> savedHistories = historyCaptor.getAllValues();


        StatusHistory approvedHistory = savedHistories.get(0);
        assertEquals(ApplicationStatus.APPROVED, approvedHistory.getStatus());
        assertEquals(ChangeType.MANUAL, approvedHistory.getChangeType());

        StatusHistory ccApprovedHistory = savedHistories.get(1);
        assertEquals(ApplicationStatus.CC_APPROVED, ccApprovedHistory.getStatus());
        assertEquals(ChangeType.AUTOMATIC, ccApprovedHistory.getChangeType());
    }

}
