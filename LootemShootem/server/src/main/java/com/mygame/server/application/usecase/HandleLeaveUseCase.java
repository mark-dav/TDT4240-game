package com.mygame.server.application.usecase;

import com.mygame.server.application.service.MatchService;
import com.mygame.server.util.RateLimiter;

/** Cleans up all server-side state when a player disconnects. */
public final class HandleLeaveUseCase {

    private final MatchService  matchService;
    private final RateLimiter   rateLimiter;

    public HandleLeaveUseCase(MatchService matchService, RateLimiter rateLimiter) {
        this.matchService = matchService;
        this.rateLimiter  = rateLimiter;
    }

    public void execute(String playerId) {
        if (playerId == null) return;
        matchService.removePlayer(playerId);
        rateLimiter.remove(playerId);
    }
}
