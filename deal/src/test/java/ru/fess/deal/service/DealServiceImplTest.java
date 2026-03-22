package ru.fess.deal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.fess.deal.client.CalculatorClient;
import ru.fess.deal.dto.*;
import ru.fess.deal.entity.*;
import ru.fess.deal.enums.ApplicationStatus;
import ru.fess.deal.enums.ChangeType;
import ru.fess.deal.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        verify(client).getLoanOffer(any());
    }

    @Test
    void calculateOffers_shouldReturnEmptyList_whenCalculatorReturnsEmpty() {
        // Arrange
        LoanStatementRequestDto request = createValidRequestDto();

        when(client.getLoanOffer(any())).thenReturn(List.of());
        when(statementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<LoanOfferDto> result = dealService.calculateOffers(request);

        // Assert
        assertTrue(result.isEmpty());
        verify(statementRepository).save(any()); // Statement всё равно должен сохраниться
    }

    @Test
    void calculateOffers_shouldThrowException_whenInvalidEmail() {
        LoanStatementRequestDto request = createValidRequestDto();
        request.setEmail("invalid-email");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dealService.calculateOffers(request)
        );

        assertTrue(exception.getMessage().contains("Invalid email"));

        verify(passportRepository, never()).save(any());
        verify(clientRepository, never()).save(any());
        verify(statementRepository, never()).save(any());
        verify(client, never()).getLoanOffer(any());
    }

    @Test
    void calculateOffers_shouldCreateClientWithPassport_whenNewClient() {
        LoanStatementRequestDto request = createValidRequestDto();
        UUID savedStatementId = UUID.randomUUID();

        when(client.getLoanOffer(any())).thenReturn(List.of(LoanOfferDto.builder().statementId(savedStatementId).build()));
        when(statementRepository.save(any())).thenAnswer(invocation -> {
            Statement stmt = invocation.getArgument(0);
            stmt.setId(savedStatementId);
            return stmt;
        });
        when(passportRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(clientRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        dealService.calculateOffers(request);

        verify(passportRepository).save(argThat(passport ->
                passport.getSeries().equals("1234") &&
                        passport.getNumber().equals("123456")));

        verify(clientRepository).save(argThat(client ->
                client.getEmail().equals("test@test.com") &&
                        client.getFirstName().equals("Alex")));
    }

    @Test
    void calculateOffers_shouldAddStatusHistory_whenStatementCreated() {
        LoanStatementRequestDto request = createValidRequestDto();
        UUID savedStatementId = UUID.randomUUID();

        when(client.getLoanOffer(any())).thenReturn(List.of(LoanOfferDto.builder().statementId(savedStatementId).build()));
        when(statementRepository.save(any())).thenAnswer(invocation -> {
            Statement stmt = invocation.getArgument(0);
            stmt.setId(savedStatementId);
            stmt.setStatusHistory(List.of(
                    StatementStatusHistoryDto.builder()
                            .status(ApplicationStatus.PREAPPROVAL)
                            .time(LocalDateTime.now())
                            .changeType(ChangeType.AUTOMATIC)
                            .build()
            ));
            return stmt;
        });

        List<LoanOfferDto> result = dealService.calculateOffers(request);

        assertNotNull(result);
        verify(statementRepository).save(argThat(stmt ->
                stmt.getStatusHistory() != null &&
                        !stmt.getStatusHistory().isEmpty() &&
                        stmt.getStatusHistory().get(0).getStatus() == ApplicationStatus.PREAPPROVAL));
    }

    @Test
    void selectOffer_shouldThrowException_whenStatementNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        LoanOfferDto offer = LoanOfferDto.builder().build();

        when(statementRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dealService.selectOffer(nonExistentId, offer)
        );

        assertTrue(exception.getMessage().contains("Statement not found"));

        verify(statementRepository, never()).save(any());
        verify(creditRepository, never()).save(any());
        verify(client, never()).calculateCredit(any());
    }

    @Test
    void selectOffer_shouldPropagateException_whenCalculatorFails() {
        UUID statementId = UUID.randomUUID();

        Passport passport = Passport.builder()
                .series("1234")
                .number("123456")
                .build();

        Client clientEntity = Client.builder()
                .firstName("Alex")
                .lastName("Petrov")
                .middleName("Jovanovich")
                .birthDate(LocalDate.of(2000, 5, 1))
                .passport(passport)  // ✅ Важно!
                .build();

        Statement statement = Statement.builder()
                .id(statementId)
                .client(clientEntity)
                .status(ApplicationStatus.PREAPPROVAL)
                .statusHistory(new ArrayList<>())  // ✅ Чтобы не было NPE при add()
                .build();

        LoanOfferDto offer = LoanOfferDto.builder()
                .requestedAmount(BigDecimal.valueOf(500000))
                .term(24)
                .build();

        when(statementRepository.findById(statementId)).thenReturn(Optional.of(statement));

        when(client.calculateCredit(any(ScoringDataDto.class)))
                .thenThrow(new RuntimeException("Calculator service unavailable"));

        when(statementRepository.save(any(Statement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> dealService.selectOffer(statementId, offer)
        );

        assertEquals("Calculator service unavailable", exception.getMessage());

        verify(creditRepository, never()).save(any());

        verify(statementRepository).save(statement);

        verify(client).calculateCredit(any(ScoringDataDto.class));
    }

    @Test
    void selectOffer_shouldAddTwoHistoryEntries() {
        UUID statementId = UUID.randomUUID();
        LoanOfferDto offer = LoanOfferDto.builder()
                .requestedAmount(BigDecimal.valueOf(200000))
                .term(18)
                .build();

        Statement statement = Statement.builder()
                .id(statementId)
                .client(Client.builder().passport(Passport.builder().build()).build())
                .statusHistory(new ArrayList<>())
                .build();

        when(statementRepository.findById(statementId)).thenReturn(Optional.of(statement));
        when(client.calculateCredit(any())).thenReturn(CreditDto.builder().build());
        when(statementRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(creditRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        dealService.selectOffer(statementId, offer);

        List<StatementStatusHistoryDto> history = statement.getStatusHistory();

        assertEquals(2, history.size(), "Should add exactly 2 history entries");

        StatementStatusHistoryDto first = history.get(0);
        assertEquals(ApplicationStatus.APPROVED, first.getStatus());
        assertEquals(ChangeType.MANUAL, first.getChangeType());
        assertNotNull(first.getTime());

        StatementStatusHistoryDto second = history.get(1);
        assertEquals(ApplicationStatus.CC_APPROVED, second.getStatus());
        assertEquals(ChangeType.AUTOMATIC, second.getChangeType());
        assertNotNull(second.getTime());

        assertTrue(!second.getTime().isBefore(first.getTime()));
    }


    private LoanStatementRequestDto createValidRequestDto() {
        LoanStatementRequestDto dto = new LoanStatementRequestDto();
        dto.setAmount(BigDecimal.valueOf(500000));
        dto.setTerm(24);
        dto.setFirstName("Alex");
        dto.setLastName("Petrov");
        dto.setMiddleName("Jovanovich");
        dto.setEmail("test@test.com");
        dto.setBirthDate(LocalDate.of(2000, 5, 1));
        dto.setPassportSeries("1234");
        dto.setPassportNumber("123456");
        return dto;
    }
}
