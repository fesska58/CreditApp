package ru.fess.deal.dto;

import liquibase.change.Change;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.fess.deal.enums.ApplicationStatus;
import ru.fess.deal.enums.ChangeType;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementStatusHistoryDto {
    private ApplicationStatus status;
    private Instant time;
    private ChangeType changeType;
}
