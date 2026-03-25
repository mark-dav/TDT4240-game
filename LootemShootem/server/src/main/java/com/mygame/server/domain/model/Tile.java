package com.mygame.server.domain.model;

import com.mygame.shared.dto.TileType;

/**
 * Centralises all tile-behaviour rules so adding a new tile type only
 * requires editing this class (Modifiability tactic: localise change).
 */
public final class Tile {

    private Tile() {}

    public static boolean isWalkable(TileType type) {
        return type == TileType.FLOOR || type == TileType.TRAP;
    }

    public static boolean blocksProjectile(TileType type) {
        return type == TileType.WALL;
    }

    /** Damage per second while standing on this tile (0 if none). */
    public static float damagePerSecond(TileType type) {
        return type == TileType.TRAP ? 10f : 0f;
    }
}
