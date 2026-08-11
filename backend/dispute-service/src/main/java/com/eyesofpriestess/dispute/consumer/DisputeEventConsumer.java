package com.eyesofpriestess.dispute.consumer;

import com.eyesofpriestess.dispute.event.RoomBrokenEvent;
import com.eyesofpriestess.dispute.service.DisputeService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * DisputeEventConsumer — Listens for RabbitMQ events and triggers case opening.
 *
 * Room.broken ──> Opens a new DisputeCase automatically.
 */
@ApplicationScoped
public class DisputeEventConsumer {

    private static final Logger LOG = Logger.getLogger(DisputeEventConsumer.class);

    @Inject DisputeService DisputeService;

    @Incoming("Room-broken")
    public CompletionStage<Void> onCovenantBroken(Message<RoomBrokenEvent> message) {
        RoomBrokenEvent event = message.getPayload();
        LOG.infof("[JUDGMENT-CONSUMER] Received Room-broken event: Room=%s reason=%s",
                event.roomId, event.reason);

        UUID roomId    = UUID.fromString(event.roomId);
        UUID initiatorId   = UUID.fromString(event.initiatorId);
        UUID counterpartyId = event.counterpartyId != null && !event.counterpartyId.isBlank()
                ? UUID.fromString(event.counterpartyId) : initiatorId;

        return DisputeService.openCase(roomId, initiatorId, counterpartyId,
                        event.reason, null, null)
                .invoke(c -> LOG.infof("[JUDGMENT-CONSUMER] Case opened: caseId=%s Room=%s", c.id, event.roomId))
                .onFailure().invoke(err -> LOG.errorf(err, "[JUDGMENT-CONSUMER] Failed to open case for Room=%s", event.roomId))
                .onFailure().recoverWithNull()
                .subscribeAsCompletionStage()
                .thenCompose(ignored -> message.ack());
    }
}
