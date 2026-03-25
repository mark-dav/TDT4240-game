package com.mygame.server.application.usecase;

import com.mygame.server.application.service.MatchService;
import com.mygame.server.util.RateLimiter;
import com.mygame.shared.protocol.messages.InputMessage;

/** Submits a player's input after rate-limit check. */
public final class HandleInputUseCase {

    private final MatchService matchService;
    private final RateLimiter  rateLimiter;

    public HandleInputUseCase(MatchService matchService, RateLimiter rateLimiter) {
        this.matchService = matchService;
        this.rateLimiter  = rateLimiter;
    }

    /** @return false if the request was rate-limited and dropped */
    public boolean execute(String playerId, InputMessage input) {
        if (!rateLimiter.allow(playerId)) {
            System.err.println("[RATE] Dropped input from " + playerId);
            return false;
        }
        matchService.submitInput(playerId, input);
        return true;
    }
}
