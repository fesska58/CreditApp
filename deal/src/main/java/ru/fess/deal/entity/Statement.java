package ru.fess.deal.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;

import jakarta.persistence.*;
import lombok.*;
import ru.fess.deal.dto.LoanOfferDto;
import ru.fess.deal.dto.StatementStatusHistoryDto;
import ru.fess.deal.enums.ApplicationStatus;
import org.hibernate.annotations.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "statement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Statement {

    @Id
    @GeneratedValue
    @Column(name = "statement_id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @OneToOne
    @JoinColumn(name = "credit_id")
    private Credit credit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ApplicationStatus status;

    private LocalDateTime creationDate;
    private LocalDateTime signDate;
    private String sesCode;

    @Column(columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private LoanOfferDto appliedOffer;

    @Column(columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private List<StatementStatusHistoryDto> statusHistory;
}
