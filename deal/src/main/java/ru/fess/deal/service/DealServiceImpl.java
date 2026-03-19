package ru.fess.deal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.fess.deal.client.CalculatorClient;
import ru.fess.deal.dto.CreditDto;
import ru.fess.deal.dto.LoanOfferDto;
import ru.fess.deal.dto.LoanStatementRequestDto;
import ru.fess.deal.dto.ScoringDataDto;
import ru.fess.deal.entity.*;
import ru.fess.deal.enums.ApplicationStatus;
import ru.fess.deal.enums.ChangeType;
import ru.fess.deal.repository.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService{

    private final CalculatorClient client;
    private final ClientRepository clientRepository;
    private final StatementRepository statementRepository;
    private final PassportRepository passportRepository;
    private final CreditRepository creditRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    @Override
    public List<LoanOfferDto> calculateOffers(LoanStatementRequestDto request) {
        log.info("LoanStatementRequestDto {}", request);

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

        Statement statementEntity = Statement.builder()
                .client(clientEntity)
                .status(ApplicationStatus.PREAPPROVAL)
                .build();
        Statement savedStatement = statementRepository.save(statementEntity);  // ← важно!
        UUID statementId = savedStatement.getId();

        log.debug("Statement saved with ID: {}", statementId);

        StatusHistory statusHistory = StatusHistory.builder()
                .statement(statementEntity)
                .status(ApplicationStatus.PREAPPROVAL)
                .time(Instant.now())
                .changeType(ChangeType.AUTOMATIC)
                .build();
        statusHistoryRepository.save(statusHistory);

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


        statement.setStatus(ApplicationStatus.APPROVED);
        statement.setAppliedOffer(offer);
        log.debug("statement {}", statement);

        StatusHistory approvedHistory = StatusHistory.builder()
                .statement(statement)
                .status(ApplicationStatus.APPROVED)
                .time(Instant.now())
                .changeType(ChangeType.MANUAL)
                .build();
        statusHistoryRepository.save(approvedHistory);

        List<StatusHistory> histories = new ArrayList<StatusHistory>();
        histories.add(approvedHistory);
        statement.setStatusHistory(histories);

        statement.setAppliedOffer(offer);
        statementRepository.save(statement);

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

        CreditDto creditDto = client.calculateCredit(scoringDataDto);

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

        StatusHistory ccApprovedHistory = StatusHistory.builder()
                .statement(statement)
                .status(ApplicationStatus.CC_APPROVED)
                .time(Instant.now())
                .changeType(ChangeType.AUTOMATIC)
                .build();
        statusHistoryRepository.save(ccApprovedHistory);
        histories.add(ccApprovedHistory);
        log.debug("ccApprovedHistory {}", ccApprovedHistory);

        statement.setStatusHistory(histories);
        statementRepository.save(statement);
        log.debug("List histories {}", histories);
    }
}
