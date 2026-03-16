package ru.fess.deal.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.fess.deal.enums.ApplicationStatus;

import java.time.Instant;
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

    private Instant creationDate;
    private Instant signDate;
    private String sesCode;

    @Column(columnDefinition = "jsonb")
    private String appliedOffer;

    @OneToMany(mappedBy = "statement", cascade = CascadeType.ALL)
    private List<StatusHistory> statusHistory;
}
