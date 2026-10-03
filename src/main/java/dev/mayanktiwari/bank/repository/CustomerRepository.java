package dev.mayanktiwari.bank.repository;

import dev.mayanktiwari.bank.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
