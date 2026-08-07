package com.eyesofpriestess.judgment.consumer;

import com.eyesofpriestess.judgment.event.CovenantBrokenEvent;
import com.eyesofpriestess.judgment.service.JudgmentService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * JudgmentEventConsumer — Listens for RabbitMQ events and triggers case opening.
 *
 * covenant.broken ──> Opens a new JudgmentCase automatically.
 */
@ApplicationScoped
public class JudgmentEventConsumer {

    private static final Logger LOG = Logger.getLogger(JudgmentEventConsumer.class);

    @Inject JudgmentService judgmentService;

    @Incoming("covenant-broken")
    public CompletionStage<Void> onCovenantBroken(Message<CovenantBrokenEvent> message) {
        CovenantBrokenEvent event = message.getPayload();
        LOG.infof("[JUDGMENT-CONSUMER] Received covenant-broken event: covenant=%s reason=%s",
                event.covenantId, event.reason);

        UUID covenantId    = UUID.fromString(event.covenantId);
        UUID initiatorId   = UUID.fromString(event.initiatorId);
        UUID counterpartyId = event.counterpartyId != null && !event.counterpartyId.isBlank()
                ? UUID.fromString(event.counterpartyId) : initiatorId;

        return judgmentService.openCase(covenantId, initiatorId, counterpartyId,
                        event.reason, null, null)
                .invoke(c -> LOG.infof("[JUDGMENT-CONSUMER] Case opened: caseId=%s covenant=%s", c.id, event.covenantId))
                .onFailure().invoke(err -> LOG.errorf(err, "[JUDGMENT-CONSUMER] Failed to open case for covenant=%s", event.covenantId))
                .onFailure().recoverWithNull()
                .subscribeAsCompletionStage()
                .thenCompose(ignored -> message.ack());
    }
}
