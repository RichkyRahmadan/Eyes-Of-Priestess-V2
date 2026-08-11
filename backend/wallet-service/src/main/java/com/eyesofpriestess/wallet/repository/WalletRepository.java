package com.eyesofpriestess.wallet.repository;

import com.eyesofpriestess.wallet.entity.Wallet;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

/** WalletRepository — Data access for Wallet entities. */
@ApplicationScoped
public class WalletRepository implements PanacheRepositoryBase<Wallet, UUID> {

    public Uni<Optional<Wallet>> findByPilgrimId(UUID userId) {
        return find("userId", userId)
                .firstResult()
                .map(Optional::ofNullable);
    }

    /** Find Wallet with pessimistic read for balance operations. */
    public Uni<Optional<Wallet>> findByPilgrimIdForUpdate(UUID userId) {
        return find("userId = ?1 AND status = ?2",
                userId, Wallet.TreasuryStatus.ACTIVE)
                .firstResult()
                .map(Optional::ofNullable);
    }

    public Uni<Optional<Wallet>> findByIdSafe(UUID id) {
        return findById(id).map(Optional::ofNullable);
    }
}
