package ru.fess.deal.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.fess.deal.dto.PaymentScheduleElementDto;

import java.math.BigDecimal;
import java.util.List;
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
    @Convert(converter = PaymentScheduleElementDto.class)
    private List<PaymentScheduleElementDto> paymentSchedule;

    private boolean insuranceEnabled;
    private boolean salaryClient;
}
