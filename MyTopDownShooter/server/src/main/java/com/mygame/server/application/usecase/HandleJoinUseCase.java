package com.mygame.server.application.usecase;

import com.mygame.server.application.service.MatchService;
import com.mygame.shared.protocol.messages.JoinAccepted;

/** Encapsulates the join flow: validate → add player → return accepted payload. */
public final class HandleJoinUseCase {

    private final MatchService matchService;

    public HandleJoinUseCase(MatchService matchService) {
        this.matchService = matchService;
    }

    /**
     * @return the {@link JoinAccepted} payload to send back, or {@code null} if
     *         the username is invalid.
     */
    public JoinAccepted execute(String username) {
        if (username == null || username.isBlank()) return null;
        String playerId = matchService.addPlayer(username.trim());
        return matchService.buildJoinAccepted(playerId);
    }
}
