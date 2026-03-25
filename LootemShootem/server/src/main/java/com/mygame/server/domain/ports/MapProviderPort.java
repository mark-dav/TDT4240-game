package com.mygame.server.domain.ports;

import com.mygame.server.domain.model.ServerGameState;

/**
 * Port (interface) for map loading.
 * Decouples the domain from the specific file format / storage mechanism.
 */
public interface MapProviderPort {
    ServerGameState provide(String mapId);
}
