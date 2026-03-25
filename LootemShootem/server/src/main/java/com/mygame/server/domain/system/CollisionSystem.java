package com.mygame.server.domain.system;

import com.mygame.server.domain.model.PlayerState;
import com.mygame.server.domain.model.ServerGameState;
import com.mygame.shared.util.Vec2;

/**
 * Handles player movement, tile collision, facing updates, and speed-boost decay.
 * Extracted from MatchService to keep each system focused on one concern.
 */
public final class CollisionSystem {

    private final ServerGameState state;

    public CollisionSystem(ServerGameState state) {
        this.state = state;
    }

    /**
     * Integrates the player's position for one tick, applying diagonal normalisation,
     * tile collision (axis-separated), and facing from the aim vector.
     */
    public void applyMovement(PlayerState p, Vec2 move, Vec2 aim, float dt) {
        // Normalise move so diagonal is not faster
        float mx = clamp(move.x, -1f, 1f);
        float my = clamp(move.y, -1f, 1f);
        float len2 = mx * mx + my * my;
        if (len2 > 1f) {
            float inv = (float) (1.0 / Math.sqrt(len2));
            mx *= inv;
            my *= inv;
        }

        // Update facing when aim is non-trivial
        float ax = clamp(aim.x, -1f, 1f);
        float ay = clamp(aim.y, -1f, 1f);
        float alen2 = ax * ax + ay * ay;
        if (alen2 > 0.0001f) {
            float inv = (float) (1.0 / Math.sqrt(alen2));
            p.facing = new Vec2(ax * inv, ay * inv);
        }

        float vx = mx * p.moveSpeed;
        float vy = my * p.moveSpeed;
        Vec2 newPos = new Vec2(p.pos.x + vx * dt, p.pos.y + vy * dt);
        p.pos = state.collidePlayer(p.pos, newPos);
        p.vel = new Vec2(vx, vy);
    }

    /** Counts down the speed-boost timer and resets move speed when it expires. */
    public void tickSpeedBoost(PlayerState p, float dt) {
        if (p.speedBoostTimer > 0f) {
            p.speedBoostTimer = Math.max(0f, p.speedBoostTimer - dt);
            if (p.speedBoostTimer == 0f) {
                p.moveSpeed = PlayerState.BASE_MOVE_SPEED;
            }
        }
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
