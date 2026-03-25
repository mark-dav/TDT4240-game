package com.mygame.server.application.usecase;

import com.mygame.server.application.service.MatchService;
import com.mygame.server.presentation.websocket.GameWebSocketServer;
import com.mygame.shared.protocol.MessageCodec;
import com.mygame.shared.protocol.messages.SnapshotMessage;

/** Builds a snapshot from the current game state and broadcasts it to all clients. */
public final class BroadcastSnapshotUseCase {

    private final MatchService          matchService;
    private final GameWebSocketServer   wsServer;
    private final MessageCodec          codec = new MessageCodec();

    public BroadcastSnapshotUseCase(MatchService matchService, GameWebSocketServer wsServer) {
        this.matchService = matchService;
        this.wsServer     = wsServer;
    }

    public void execute() {
        try {
            SnapshotMessage msg = new SnapshotMessage(matchService.buildSnapshot());
            wsServer.broadcastText(codec.encode(msg));
        } catch (Exception e) {
            System.err.println("[BROADCAST] Snapshot failed: " + e.getMessage());
        }
    }
}
