package com.eyesofpriestess.wallet.consumer;

import com.eyesofpriestess.wallet.event.CovenantFulfilledEvent;
import com.eyesofpriestess.wallet.event.JudgmentResolvedEvent;
import com.eyesofpriestess.wallet.service.VaultService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import java.util.UUID;

/** VaultEventConsumer — RabbitMQ event consumer for asynchronous escrow settlement. */
@ApplicationScoped
public class VaultEventConsumer {

    private static final Logger LOG = Logger.getLogger(VaultEventConsumer.class);

    @Inject
    VaultService vaultService;

    @Incoming("covenant-fulfilled")
    public Uni<Void> onCovenantFulfilled(CovenantFulfilledEvent event) {
        LOG.infof("[RABBITMQ] Received covenant-fulfilled: covenantId=%s, buyer=%s, seller=%s, amount=%s",
                event.covenantId, event.buyerId, event.sellerId, event.amount);

        UUID buyerId = UUID.fromString(event.buyerId);
        UUID sellerId = UUID.fromString(event.sellerId);

        return vaultService.releaseEscrow(buyerId, sellerId, event.amount, event.covenantId)
                .onItem().invoke(() -> LOG.infof("[RABBITMQ] Escrow released successfully for covenant=%s", event.covenantId))
                .onFailure().invoke(err -> LOG.errorf(err, "[RABBITMQ] Failed to release escrow for covenant=%s", event.covenantId));
    }

    @Incoming("judgment-resolved")
    public Uni<Void> onJudgmentResolved(JudgmentResolvedEvent event) {
        LOG.infof("[RABBITMQ] Received judgment-resolved: disputeId=%s, winner=%s",
                event.disputeId, event.winnerRole);

        UUID buyerId = UUID.fromString(event.buyerId);
        UUID sellerId = UUID.fromString(event.sellerId);

        if ("BUYER".equalsIgnoreCase(event.winnerRole)) {
            return vaultService.refundEscrow(buyerId, event.buyerRefundAmount, event.covenantId);
        } else if ("SELLER".equalsIgnoreCase(event.winnerRole)) {
            return vaultService.releaseEscrow(buyerId, sellerId, event.sellerPayoutAmount, event.covenantId);
        } else {
            // SPLIT decision
            return vaultService.refundEscrow(buyerId, event.buyerRefundAmount, event.covenantId)
                    .flatMap(v -> vaultService.releaseEscrow(buyerId, sellerId, event.sellerPayoutAmount, event.covenantId));
        }
    }
}
