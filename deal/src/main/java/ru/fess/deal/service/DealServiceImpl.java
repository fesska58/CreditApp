package ru.fess.deal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.fess.deal.client.CalculatorClient;
import ru.fess.deal.dto.LoanOfferDto;
import ru.fess.deal.dto.LoanStatementRequestDto;
import ru.fess.deal.entity.*;
import ru.fess.deal.enums.ApplicationStatus;
import ru.fess.deal.enums.ChangeType;
import ru.fess.deal.repository.*;

import java.time.Instant;
import java.util.List;

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
        statementRepository.save(statementEntity);

        StatusHistory statusHistory = StatusHistory.builder()
                .statement(statementEntity)
                .status(ApplicationStatus.PREAPPROVAL)
                .time(Instant.now())
                .changeType(ChangeType.AUTOMATIC)
                .build();
        statusHistoryRepository.save(statusHistory);

        List<LoanOfferDto> offers = client.getLoanOffer(request);
        offers.forEach(offersEntity -> {
            offersEntity.setStatementId(statementEntity.getId());
        });

        return offers;
    }



    @Override
    public void selectOffer(LoanOfferDto offer) {
        Statement statement = statementRepository
                .findById(offer.getStatementId())
                .orElse(null);

        Credit credit = Credit.builder()
                .amount(offer.getRequestedAmount())
                .term(offer.getTerm())
                .monthlyPayment(offer.getMonthlyPayment())
                .rate(offer.getRate())
                .insuranceEnabled(offer.getIsInsuranceEnabled())
                .salaryClient(offer.getIsSalaryClient())
                .build();

        creditRepository.save(credit);

        assert statement != null;
        statement.setCredit(credit);
        statement.setStatus(ApplicationStatus.APPROVED);
        statementRepository.save(statement);

        StatusHistory statusHistory = StatusHistory.builder()
                .status(ApplicationStatus.APPROVED)
                .time(Instant.now())
                .changeType(ChangeType.AUTOMATIC)
                .statement(statement)
                .build();
        statusHistoryRepository.save(statusHistory);

    }
}
