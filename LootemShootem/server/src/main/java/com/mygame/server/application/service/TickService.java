package com.mygame.server.application.service;

import com.mygame.server.application.usecase.BroadcastSnapshotUseCase;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Fixed-timestep tick loop (authoritative server).
 * Tick rate is taken from {@link com.mygame.server.config.ServerConfig}
 * so it can be changed in {@code server.conf} without recompiling.
 */
public final class TickService {

    private final MatchService              matchService;
    private final BroadcastSnapshotUseCase  broadcastUseCase;
    private final int                       tickHz;
    private final ScheduledExecutorService  exec =
            Executors.newSingleThreadScheduledExecutor();

    public TickService(MatchService matchService,
                       BroadcastSnapshotUseCase broadcastUseCase,
                       int tickHz) {
        this.matchService     = matchService;
        this.broadcastUseCase = broadcastUseCase;
        this.tickHz           = tickHz;
    }

    public void start() {
        float dt       = 1.0f / tickHz;
        long periodMs  = Math.round(1000.0 / tickHz);

        exec.scheduleAtFixedRate(() -> {
            try {
                matchService.tick(dt);
                broadcastUseCase.execute();
            } catch (Exception e) {
                System.err.println("[TICK] error: " + e);
            }
        }, 0, periodMs, TimeUnit.MILLISECONDS);

        System.out.println("[SERVER] Tick loop started @ " + tickHz + " Hz");
    }
}
