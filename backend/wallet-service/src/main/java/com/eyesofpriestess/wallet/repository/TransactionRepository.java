package com.eyesofpriestess.wallet.repository;

import com.eyesofpriestess.wallet.entity.Transaction;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** TransactionRepository — Append-only access to transaction history. */
@ApplicationScoped
public class TransactionRepository implements PanacheRepositoryBase<Transaction, UUID> {

    /** Paginated Transaction history for a Wallet, newest first. */
    public Uni<List<Transaction>> findByTreasuryId(UUID treasuryId, int page, int size) {
        return find("treasuryId = ?1", Sort.by("createdAt").descending(), treasuryId)
                .page(Page.of(page, size))
                .list();
    }

    public Uni<Long> countByTreasuryId(UUID treasuryId) {
        return count("treasuryId", treasuryId);
    }

    public Uni<Optional<Transaction>> findByCovenantKey(String covenantKey) {
        return find("covenantKey", covenantKey)
                .firstResult()
                .map(Optional::ofNullable);
    }
}
