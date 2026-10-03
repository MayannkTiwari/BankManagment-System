package dev.mayanktiwari.bank.repository;

import dev.mayanktiwari.bank.domain.LedgerEntry;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    boolean existsByAccountIdAndRequestId(Long accountId, UUID requestId);

    List<LedgerEntry> findTop10ByAccountIdOrderByCreatedAtDescIdDesc(Long accountId);

    Page<LedgerEntry> findByAccountIdOrderByCreatedAtDescIdDesc(Long accountId, Pageable pageable);
}
