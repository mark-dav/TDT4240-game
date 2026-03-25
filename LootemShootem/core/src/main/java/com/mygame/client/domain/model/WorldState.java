package com.mygame.client.domain.model;

import com.mygame.shared.dto.GameSnapshotDto;
import com.mygame.shared.dto.MapDto;
import com.mygame.shared.dto.PlayerDto;

import java.util.concurrent.atomic.AtomicReference;

public final class WorldState {

    private volatile String                        localPlayerId;
    private final AtomicReference<MapDto>          map  = new AtomicReference<>();
    private final AtomicReference<GameSnapshotDto> snap = new AtomicReference<>();

    public void setLocalPlayerId(String id)      { this.localPlayerId = id; }
    public void setMap(MapDto m)                 { map.set(m); }
    public void applySnapshot(GameSnapshotDto s) { snap.set(s); }

    public String          getLocalPlayerId() { return localPlayerId; }
    public MapDto          getMap()           { return map.get(); }
    public GameSnapshotDto getSnapshot()      { return snap.get(); }

    public PlayerDto getLocalPlayer() {
        GameSnapshotDto s = snap.get();
        if (s == null || localPlayerId == null) return null;
        for (PlayerDto p : s.players) {
            if (localPlayerId.equals(p.playerId)) return p;
        }
        return null;
    }
}
