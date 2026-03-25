package com.mygame.server.data.map;

import com.mygame.server.domain.model.ServerGameState;
import com.mygame.server.domain.ports.MapProviderPort;

/**
 * Adapter that satisfies {@link MapProviderPort} by loading JSON map files
 * from the classpath via {@link MapParser}.
 */
public final class MapProvider implements MapProviderPort {

    @Override
    public ServerGameState provide(String mapId) {
        String resource = "maps/" + mapId + ".json";
        try {
            ServerGameState state = MapParser.load(resource);
            System.out.println("[MAP] Loaded '" + mapId + "' ("
                    + state.width + "x" + state.height + ")");
            return state;
        } catch (Exception e) {
            System.err.println("[MAP] Could not load " + resource
                    + ": " + e.getMessage() + " — using procedural fallback");
            return ServerGameState.createWithBorderWalls(mapId, 30, 20);
        }
    }
}
