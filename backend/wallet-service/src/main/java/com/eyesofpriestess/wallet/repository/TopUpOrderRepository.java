package com.eyesofpriestess.wallet.repository;

import com.eyesofpriestess.wallet.entity.TopUpOrder;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

/** TopUpOrderRepository — Access to Xendit top-up requests. */
@ApplicationScoped
public class TopUpOrderRepository implements PanacheRepositoryBase<TopUpOrder, UUID> {

    public Uni<Optional<TopUpOrder>> findByGatewayTransactionId(String txId) {
        return find("gatewayTransactionId", txId)
                .firstResult()
                .map(Optional::ofNullable);
    }

    public Uni<Optional<TopUpOrder>> findByCovenantKey(String key) {
        return find("covenantKey", key)
                .firstResult()
                .map(Optional::ofNullable);
    }

    public Uni<Optional<TopUpOrder>> findByIdSafe(UUID id) {
        return findById(id).map(Optional::ofNullable);
    }
}
