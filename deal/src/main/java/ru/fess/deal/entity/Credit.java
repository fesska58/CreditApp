package ru.fess.deal.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "credit_id", columnDefinition = "uuid")
    private UUID id;

    private BigDecimal amount;
    private Integer term;
    private BigDecimal monthlyPayment;
    private BigDecimal rate;
    private BigDecimal psk;

    @Column(columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private List<PaymentScheduleElementDto> paymentSchedule;

    private Boolean insuranceEnabled;
    private Boolean salaryClient;
}
