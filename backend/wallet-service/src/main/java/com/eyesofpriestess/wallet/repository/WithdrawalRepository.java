package com.eyesofpriestess.wallet.repository;

import com.eyesofpriestess.wallet.entity.Withdrawal;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

/** WithdrawalRepository — Access to bank disbursement requests. */
@ApplicationScoped
public class WithdrawalRepository implements PanacheRepositoryBase<Withdrawal, UUID> {

    public Uni<Optional<Withdrawal>> findByGatewayTransactionId(String txId) {
        return find("gatewayTransactionId", txId)
                .firstResult()
                .map(Optional::ofNullable);
    }

    public Uni<Optional<Withdrawal>> findByCovenantKey(String key) {
        return find("covenantKey", key)
                .firstResult()
                .map(Optional::ofNullable);
    }

    public Uni<Optional<Withdrawal>> findByIdSafe(UUID id) {
        return findById(id).map(Optional::ofNullable);
    }
}
