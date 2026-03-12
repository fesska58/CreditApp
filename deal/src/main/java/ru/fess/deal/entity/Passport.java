package ru.fess.deal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "passport")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "passport_uuid", columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false)
    private String series;
}
