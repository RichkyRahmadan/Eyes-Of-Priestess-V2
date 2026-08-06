package com.eyesofpriestess.vault.repository;

import com.eyesofpriestess.vault.entity.Offering;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

/** OfferingRepository — Access to Xendit top-up requests. */
@ApplicationScoped
public class OfferingRepository implements PanacheRepositoryBase<Offering, UUID> {

    public Uni<Optional<Offering>> findByGatewayTransactionId(String txId) {
        return find("gatewayTransactionId", txId)
                .firstResult()
                .map(Optional::ofNullable);
    }

    public Uni<Optional<Offering>> findByCovenantKey(String key) {
        return find("covenantKey", key)
                .firstResult()
                .map(Optional::ofNullable);
    }

    public Uni<Optional<Offering>> findByIdSafe(UUID id) {
        return findById(id).map(Optional::ofNullable);
    }
}
