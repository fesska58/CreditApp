package ru.fess.deal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fess.deal.entity.Client;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
}
