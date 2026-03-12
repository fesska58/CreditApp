package ru.fess.deal.entity;

import jakarta.persistence.*;
import lombok.*;
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

    @Column(nullable = false)
    private String status;

    private Instant time;

    @Enumerated(EnumType.STRING)
    private ChangeType changeType;

    @ManyToOne
    @JoinColumn(name = "statement_id")
    private Statement statement;
}
