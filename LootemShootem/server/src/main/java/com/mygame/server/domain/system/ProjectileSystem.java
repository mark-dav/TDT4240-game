package com.mygame.server.domain.system;

import com.mygame.server.domain.model.PlayerState;
import com.mygame.server.domain.model.ProjectileState;
import com.mygame.server.domain.model.ServerGameState;
import com.mygame.shared.util.Vec2;

/**
 * Moves all projectiles, removes expired / wall-blocked ones,
 * and applies damage + kill-credit on player hit.
 */
public final class ProjectileSystem {

    private static final float RESPAWN_SECONDS = 5f;

    private final ServerGameState state;

    public ProjectileSystem(ServerGameState state) {
        this.state = state;
    }

    public void update(float dt) {
        for (int i = state.projectiles.size() - 1; i >= 0; i--) {
            ProjectileState pr = state.projectiles.get(i);

            pr.ttlSeconds -= dt;
            if (pr.ttlSeconds <= 0f) {
                state.projectiles.remove(i);
                continue;
            }

            Vec2 newPos = new Vec2(pr.pos.x + pr.vel.x * dt, pr.pos.y + pr.vel.y * dt);
            if (state.isProjectileBlockedWorld(newPos.x, newPos.y)) {
                state.projectiles.remove(i);
                continue;
            }
            pr.pos = newPos;

            PlayerState hit = findHit(pr);
            if (hit != null) {
                hit.hp = Math.max(0f, hit.hp - pr.damage);
                state.projectiles.remove(i);
                if (hit.hp <= 0f && !hit.isDead) {
                    hit.isDead       = true;
                    hit.justDied     = true;
                    hit.respawnTimer = RESPAWN_SECONDS;
                    PlayerState killer = state.players.get(pr.ownerPlayerId);
                    if (killer != null) {
                        killer.score++;
                        String msg = killer.username + " killed " + hit.username;
                        state.killFeedQueue.add(msg);
                        System.out.println("[GAME] " + msg);
                    }
                }
            }
        }
    }

    private PlayerState findHit(ProjectileState pr) {
        for (PlayerState p : state.players.values()) {
            if (p.playerId.equals(pr.ownerPlayerId)) continue;
            if (p.hp <= 0f) continue;
            float dx = p.pos.x - pr.pos.x;
            float dy = p.pos.y - pr.pos.y;
            float r  = p.radius + pr.radius;
            if (dx * dx + dy * dy <= r * r) return p;
        }
        return null;
    }
}
