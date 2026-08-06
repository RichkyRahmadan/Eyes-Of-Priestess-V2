package com.eyesofpriestess.seal.repository;

import com.eyesofpriestess.seal.entity.Pilgrim;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

/**
 * PilgrimRepository — Reactive data access for Pilgrim entity.
 * All queries use Mutiny (Uni) for non-blocking reactive I/O.
 */
@ApplicationScoped
public class PilgrimRepository implements PanacheRepositoryBase<Pilgrim, UUID> {

    /** Find a Pilgrim by phone number (primary lookup for auth). */
    public Uni<Optional<Pilgrim>> findByPhone(String phone) {
        return find("phone", phone)
                .firstResult()
                .map(Optional::ofNullable);
    }

    /** Find a Pilgrim by email address. */
    public Uni<Optional<Pilgrim>> findByEmail(String email) {
        return find("email", email)
                .firstResult()
                .map(Optional::ofNullable);
    }

    /** Check if phone already exists (for registration validation). */
    public Uni<Boolean> existsByPhone(String phone) {
        return count("phone", phone).map(count -> count > 0);
    }

    /** Check if email already exists (for registration validation). */
    public Uni<Boolean> existsByEmail(String email) {
        return count("email", email).map(count -> count > 0);
    }

    /** Find active (non-sanctioned, non-suspended) pilgrim by phone. */
    public Uni<Optional<Pilgrim>> findActiveByPhone(String phone) {
        return find("phone = ?1 AND status = ?2", phone, Pilgrim.PilgrimStatus.ATTUNED)
                .firstResult()
                .map(Optional::ofNullable);
    }

    /** Find pilgrim by ID with null-safe wrapping. */
    public Uni<Optional<Pilgrim>> findByIdSafe(UUID id) {
        return findById(id).map(Optional::ofNullable);
    }
}
