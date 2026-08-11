package com.eyesofpriestess.auth.repository;

import com.eyesofpriestess.auth.entity.User;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

/**
 * UserRepository — Reactive data access for User entity.
 * All queries use Mutiny (Uni) for non-blocking reactive I/O.
 */
@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, UUID> {

    /** Find a User by phone number (primary lookup for auth). */
    public Uni<Optional<User>> findByPhone(String phone) {
        return find("phone", phone)
                .firstResult()
                .map(Optional::ofNullable);
    }

    /** Find a User by email address. */
    public Uni<Optional<User>> findByEmail(String email) {
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

    /** Find active (non-sanctioned, non-suspended) User by phone. */
    public Uni<Optional<User>> findActiveByPhone(String phone) {
        return find("phone = ?1 AND status = ?2", phone, User.PilgrimStatus.ATTUNED)
                .firstResult()
                .map(Optional::ofNullable);
    }

    /** Find User by ID with null-safe wrapping. */
    public Uni<Optional<User>> findByIdSafe(UUID id) {
        return findById(id).map(Optional::ofNullable);
    }
}
