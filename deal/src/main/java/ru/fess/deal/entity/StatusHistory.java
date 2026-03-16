package ru.fess.deal.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.fess.deal.enums.ApplicationStatus;
import ru.fess.deal.enums.ChangeType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusHistory {
    @Id
    @GeneratedValue
    @Column(name = "status_history_id", columnDefinition = "uuid")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    private Instant time;

    @Enumerated(EnumType.STRING)
    private ChangeType changeType;

    @ManyToOne
    @JoinColumn(name = "statement_id")
    private Statement statement;
}
