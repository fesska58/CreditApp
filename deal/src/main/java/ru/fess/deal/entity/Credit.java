package ru.fess.deal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "credit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credit_id", columnDefinition = "uuid")
    private UUID id;

    private BigDecimal amount;
    private int term;
    private BigDecimal monthlyPayment;
    private BigDecimal rate;
    private BigDecimal psk;

    @Column(columnDefinition = "jsonb")
    private String paymentSchedule;

    private boolean insuranceEnabled;
    private boolean salaryClient;
}
