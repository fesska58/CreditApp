package ru.fess.deal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fess.deal.entity.StatusHistory;

import java.util.UUID;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, UUID> {
}
