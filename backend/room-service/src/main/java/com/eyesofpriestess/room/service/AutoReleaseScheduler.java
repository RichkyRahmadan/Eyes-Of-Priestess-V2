package com.eyesofpriestess.room.service;

import com.eyesofpriestess.room.repository.RoomRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Instant;

/**
 * AutoReleaseScheduler — Scheduled task that checks for DELIVERED rooms past their
 * auto-release timeout and automatically fulfills them (payout released to seller).
 */
@ApplicationScoped
public class AutoReleaseScheduler {

    private static final Logger LOG = Logger.getLogger(AutoReleaseScheduler.class);

    @Inject RoomRepository covenantRepo;
    @Inject RoomService RoomService;

    @Scheduled(cron = "{Room.auto-release.cron:0 0 * * * ?}")
    @WithTransaction
    public Uni<Void> runAutoReleaseJob() {
        Instant now = Instant.now();
        LOG.infof("[AUTO-RELEASE JOB] Checking for delivered rooms past cutoff %s", now);

        return covenantRepo.findDeliveredPastAutoRelease(now)
                .flatMap(rooms -> {
                    if (rooms.isEmpty()) {
                        LOG.debug("[AUTO-RELEASE JOB] No rooms eligible for auto-release.");
                        return Uni.createFrom().voidItem();
                    }

                    LOG.infof("[AUTO-RELEASE JOB] Found %d rooms eligible for auto-release", rooms.size());

                    return Uni.join().all(
                            rooms.stream()
                                    .map(cov -> {
                                        LOG.infof("[AUTO-RELEASE JOB] Fulfilling Room %s (seller: %s)", cov.id, cov.sellerId);
                                        return RoomService.fulfillCovenant(null, cov.id);
                                    })
                                    .toList()
                    ).andCollectFailures().replaceWithVoid();
                })
                .onFailure().invoke(err -> LOG.errorf(err, "[AUTO-RELEASE JOB] Auto-release job encountered an error"));
    }
}
