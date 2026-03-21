package ru.fess.deal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {

    private final CalculatorClient client;
    private final ClientRepository clientRepository;
    private final StatementRepository statementRepository;
    private final PassportRepository passportRepository;
    private final CreditRepository creditRepository;

    @Override
    public List<LoanOfferDto> calculateOffers(LoanStatementRequestDto request) {
        log.info("LoanStatementRequestDto {}", request);

        validateRequest(request);

        Passport passportEntity = Passport.builder()
                .series(request.getPassportSeries())
                .number(request.getPassportNumber())
                .build();
        passportRepository.save(passportEntity);

        Client clientEntity = Client.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .middleName(request.getMiddleName())
                .email(request.getEmail())
                .birthDate(request.getBirthDate())
                .passport(passportEntity)
                .build();
        clientRepository.save(clientEntity);


        Statement savedStatement = createStatement(clientEntity);
        UUID statementId = savedStatement.getId();

        log.debug("Statement saved with ID: {}", statementId);

        List<LoanOfferDto> offers = client.getLoanOffer(request);

        List<LoanOfferDto> offersWithId = offers.stream()
                .map(offer -> {
                    log.debug("🔄 Mapping offer: oldId={}, newId={}",
                            offer.getStatementId(), statementId);
                    return offer.toBuilder()
                            .statementId(statementId)
                            .build();
                })
                .toList();

        log.info("List offers {}", offersWithId);
        return offersWithId;
    }


    @Override
    public void selectOffer(UUID statementId, LoanOfferDto offer) {
        log.info("statementId {}, LoanOfferDto {}", statementId, offer);
        Statement statement = statementRepository
                .findById(statementId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Statement not found with id: " + statementId));

        log.debug("statement {}", statement);

        statement.setStatus(ApplicationStatus.APPROVED);
        statement.setAppliedOffer(offer);

        StatementStatusHistoryDto approvedHistory = StatementStatusHistoryDto.builder()
                .status(ApplicationStatus.APPROVED)
                .time(LocalDateTime.now())
                .changeType(ChangeType.MANUAL)
                .build();

        statement.getStatusHistory().add(approvedHistory);
        statement.setAppliedOffer(offer);
        statementRepository.save(statement); // - statement save

        Client clientEntity = statement.getClient();
        Passport passport = clientEntity.getPassport();

        ScoringDataDto scoringDataDto = ScoringDataDto
                .builder()
                .amount(offer.getRequestedAmount())
                .term(offer.getTerm())
                .firstName(clientEntity.getFirstName())
                .lastName(clientEntity.getLastName())
                .middleName(clientEntity.getMiddleName())
                .birthdate(clientEntity.getBirthDate())
                .passportSeries(passport.getSeries())
                .passportNumber(passport.getNumber())
                .isInsuranceEnabled(offer.getIsInsuranceEnabled())
                .isSalaryClient(offer.getIsSalaryClient())
                .build();

        CreditDto creditDto = client.calculateCredit(scoringDataDto); // - POST запрос на /calculator/calc

        Credit credit = Credit
                .builder()
                .amount(creditDto.getAmount())
                .term(creditDto.getTerm())
                .monthlyPayment(creditDto.getMonthlyPayment())
                .rate(creditDto.getRate())
                .psk(creditDto.getPsk())
                .insuranceEnabled(creditDto.getIsInsuranceEnabled())
                .salaryClient(creditDto.getIsSalaryClient())
                .paymentSchedule(creditDto.getPaymentSchedule())
                .build();
        creditRepository.save(credit);
        log.debug("Credit {}", credit);

        statement.setCredit(credit);
        statement.setStatus(ApplicationStatus.CC_APPROVED);
        String sesCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        statement.setCreationDate(LocalDateTime.now());
        statement.setSesCode(sesCode);
        statement.setSignDate(LocalDateTime.now());

        StatementStatusHistoryDto ccApprovedHistory = StatementStatusHistoryDto.builder()
                .status(ApplicationStatus.CC_APPROVED)
                .time(LocalDateTime.now())
                .changeType(ChangeType.AUTOMATIC)
                .build();
        log.debug("ccApprovedHistory {}", ccApprovedHistory);


        statement.getStatusHistory().add(ccApprovedHistory);
        statementRepository.save(statement);
    }


    private Statement createStatement(Client client) {
        String sesCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Statement statement = Statement.builder()
                .client(client)
                .status(ApplicationStatus.PREAPPROVAL)
                .creationDate(LocalDateTime.now())
                .signDate(LocalDateTime.now())
                .sesCode(sesCode)
                .statusHistory(new ArrayList<>())
                .build();

        StatementStatusHistoryDto history = StatementStatusHistoryDto.builder()
                .status(ApplicationStatus.PREAPPROVAL)
                .time(LocalDateTime.now())
                .changeType(ChangeType.AUTOMATIC)
                .build();

        statement.getStatusHistory().add(history);

        return statementRepository.save(statement);
    }

    private void validateRequest(LoanStatementRequestDto request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (request.getTerm() == null || request.getTerm() < 6 || request.getTerm() > 60) {
            throw new IllegalArgumentException("Term must be between 6 and 60 months");
        }

        if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format: " + request.getEmail());
        }

        if (request.getBirthDate() == null || request.getBirthDate().isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("Client must be at least 18 years old");
        }

        if (request.getPassportSeries() == null || !request.getPassportSeries().matches("\\d{4}")) {
            throw new IllegalArgumentException("Invalid passport series");
        }
        if (request.getPassportNumber() == null || !request.getPassportNumber().matches("\\d{6}")) {
            throw new IllegalArgumentException("Invalid passport number");
        }
    }
}
