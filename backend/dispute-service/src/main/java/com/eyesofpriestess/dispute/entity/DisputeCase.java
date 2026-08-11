package com.eyesofpriestess.dispute.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * DisputeCase — A dispute case opened when a Room enters DISPUTED status.
 *
 * State machine:
 * OPEN ──(Oracle reviews evidence)──> DELIBERATING ──> RENDERED
 *                                                        │
 *                    ┌──────────────────────────────────┤
 *                    ▼                  ▼               ▼
 *            RELEASE_TO_COUNTERPART  REFUND_TO_INITIATOR  SPLIT
 */
@Entity
@Table(name = "judgment_cases", schema = "dispute")
public class DisputeCase extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "covenant_id", nullable = false, unique = true)
    public UUID roomId;

    @Column(name = "initiator_id", nullable = false)
    public UUID initiatorId;   // User who opened the dispute

    @Column(name = "counterparty_id", nullable = false)
    public UUID counterpartyId;

    @Column(name = "reason", nullable = false)
    public String reason;

    @Column(name = "description", columnDefinition = "TEXT")
    public String description;

    @Column(name = "initiator_evidence_url")
    public String initiatorEvidenceUrl;

    @Column(name = "counterparty_evidence_url")
    public String counterpartyEvidenceUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    public CaseStatus status = CaseStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_type", length = 30)
    public ResolutionType resolutionType;

    @Column(name = "oracle_id")
    public UUID oracleId;   // The Oracle (admin) who renders the judgment

    @Column(name = "oracle_notes", columnDefinition = "TEXT")
    public String oracleNotes;

    /** For SPLIT: percentage (0-100) released to counterparty (seller). */
    @Column(name = "split_percentage")
    public Integer splitPercentage;

    @Column(name = "opened_at", nullable = false, updatable = false)
    public Instant openedAt = Instant.now();

    @Column(name = "deliberating_at")
    public Instant deliberatingAt;

    @Column(name = "rendered_at")
    public Instant renderedAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt = Instant.now();

    public enum CaseStatus {
        OPEN,          // Dispute filed, awaiting Oracle assignment
        DELIBERATING,  // Oracle is reviewing evidence
        RENDERED       // Judgment delivered
    }

    public enum ResolutionType {
        RELEASE_TO_COUNTERPART,  // Escrow funds released to seller
        REFUND_TO_INITIATOR,     // Escrow funds refunded to buyer
        SPLIT                    // Split between buyer and seller per splitPercentage
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
