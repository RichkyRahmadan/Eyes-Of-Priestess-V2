package com.eyesofpriestess.wallet.consumer;

import com.eyesofpriestess.wallet.event.RoomCompletedEvent;
import com.eyesofpriestess.wallet.event.DisputeResolvedEvent;
import com.eyesofpriestess.wallet.service.WalletService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import java.util.UUID;

/** WalletEventConsumer — RabbitMQ event consumer for asynchronous escrow settlement. */
@ApplicationScoped
public class WalletEventConsumer {

    private static final Logger LOG = Logger.getLogger(WalletEventConsumer.class);

    @Inject
    WalletService WalletService;

    @Incoming("Room-fulfilled")
    public Uni<Void> onCovenantFulfilled(RoomCompletedEvent event) {
        LOG.infof("[RABBITMQ] Received Room-fulfilled: roomId=%s, buyer=%s, seller=%s, amount=%s",
                event.roomId, event.buyerId, event.sellerId, event.amount);

        UUID buyerId = UUID.fromString(event.buyerId);
        UUID sellerId = UUID.fromString(event.sellerId);

        return WalletService.releaseEscrow(buyerId, sellerId, event.amount, event.roomId)
                .onItem().invoke(() -> LOG.infof("[RABBITMQ] Escrow released successfully for Room=%s", event.roomId))
                .onFailure().invoke(err -> LOG.errorf(err, "[RABBITMQ] Failed to release escrow for Room=%s", event.roomId));
    }

    @Incoming("judgment-resolved")
    public Uni<Void> onJudgmentResolved(DisputeResolvedEvent event) {
        LOG.infof("[RABBITMQ] Received judgment-resolved: disputeId=%s, winner=%s",
                event.disputeId, event.winnerRole);

        UUID buyerId = UUID.fromString(event.buyerId);
        UUID sellerId = UUID.fromString(event.sellerId);

        if ("BUYER".equalsIgnoreCase(event.winnerRole)) {
            return WalletService.refundEscrow(buyerId, event.buyerRefundAmount, event.roomId);
        } else if ("SELLER".equalsIgnoreCase(event.winnerRole)) {
            return WalletService.releaseEscrow(buyerId, sellerId, event.sellerPayoutAmount, event.roomId);
        } else {
            // SPLIT decision
            return WalletService.refundEscrow(buyerId, event.buyerRefundAmount, event.roomId)
                    .flatMap(v -> WalletService.releaseEscrow(buyerId, sellerId, event.sellerPayoutAmount, event.roomId));
        }
    }
}
