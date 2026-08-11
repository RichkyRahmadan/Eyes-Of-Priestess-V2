package com.eyesofpriestess.room.service;

import com.eyesofpriestess.room.repository.CovenantRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Instant;

/**
 * AutoReleaseScheduler — Scheduled task that checks for DELIVERED covenants past their
 * auto-release timeout and automatically fulfills them (payout released to seller).
 */
@ApplicationScoped
public class AutoReleaseScheduler {

    private static final Logger LOG = Logger.getLogger(AutoReleaseScheduler.class);

    @Inject CovenantRepository covenantRepo;
    @Inject CovenantService covenantService;

    @Scheduled(cron = "{covenant.auto-release.cron:0 0 * * * ?}")
    @WithTransaction
    public Uni<Void> runAutoReleaseJob() {
        Instant now = Instant.now();
        LOG.infof("[AUTO-RELEASE JOB] Checking for delivered covenants past cutoff %s", now);

        return covenantRepo.findDeliveredPastAutoRelease(now)
                .flatMap(covenants -> {
                    if (covenants.isEmpty()) {
                        LOG.debug("[AUTO-RELEASE JOB] No covenants eligible for auto-release.");
                        return Uni.createFrom().voidItem();
                    }

                    LOG.infof("[AUTO-RELEASE JOB] Found %d covenants eligible for auto-release", covenants.size());

                    return Uni.join().all(
                            covenants.stream()
                                    .map(cov -> {
                                        LOG.infof("[AUTO-RELEASE JOB] Fulfilling covenant %s (seller: %s)", cov.id, cov.sellerId);
                                        return covenantService.fulfillCovenant(null, cov.id);
                                    })
                                    .toList()
                    ).andCollectFailures().replaceWithVoid();
                })
                .onFailure().invoke(err -> LOG.errorf(err, "[AUTO-RELEASE JOB] Auto-release job encountered an error"));
    }
}
