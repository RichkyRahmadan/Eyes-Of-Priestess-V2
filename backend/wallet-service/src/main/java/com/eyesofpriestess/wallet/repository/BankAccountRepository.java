package com.eyesofpriestess.wallet.repository;

import com.eyesofpriestess.wallet.entity.BankAccount;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class BankAccountRepository implements PanacheRepositoryBase<BankAccount, UUID> {

    public Uni<List<BankAccount>> findActiveByUserId(UUID userId) {
        return list("userId = ?1 and deletedAt is null order by isPrimary desc, createdAt desc", userId);
    }

    public Uni<BankAccount> findByIdAndUser(UUID id, UUID userId) {
        return find("id = ?1 and userId = ?2 and deletedAt is null", id, userId).firstResult();
    }

    public Uni<Void> clearPrimary(UUID userId) {
        return update("isPrimary = false, updatedAt = ?1 where userId = ?2 and deletedAt is null", Instant.now(), userId)
                .replaceWithVoid();
    }
}
