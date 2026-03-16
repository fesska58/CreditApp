package ru.fess.deal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fess.deal.entity.Passport;

import java.util.UUID;

public interface PassportRepository extends JpaRepository<Passport, UUID> {
}
