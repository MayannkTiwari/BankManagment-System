package dev.mayanktiwari.bank.repository;

import dev.mayanktiwari.bank.domain.Account;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByCardNumber(String cardNumber);

    boolean existsByCardNumber(String cardNumber);

    /** Row lock for anything that changes a balance or the PIN attempt counter. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.cardNumber = :cardNumber")
    Optional<Account> findByCardNumberForUpdate(@Param("cardNumber") String cardNumber);
}
