package com.mygame.server.domain.system;

import com.mygame.server.data.weapon.WeaponRegistry;
import com.mygame.server.domain.model.PlayerState;
import com.mygame.server.domain.model.ProjectileState;
import com.mygame.server.domain.model.ServerGameState;
import com.mygame.shared.dto.WeaponType;
import com.mygame.shared.protocol.messages.InputMessage;
import com.mygame.shared.util.Vec2;

import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates one authoritative simulation tick by delegating to the
 * individual domain systems and handling cross-cutting concerns
 * (respawn, trap damage, shooting, weapon switch).
 *
 * Adding a new game mechanic means either extending an existing system
 * or adding a new one — MatchService.tick() stays unchanged.
 */
public final class ServerWorldStepSystem {

    private static final float RESPAWN_SECONDS = 5f;

    private final ServerGameState  state;
    private final WeaponRegistry   weaponRegistry;
    private final CollisionSystem  collisionSystem;
    private final ProjectileSystem projectileSystem;
    private final PickupSpawnSystem pickupSpawnSystem;

    public ServerWorldStepSystem(ServerGameState state,
                                 WeaponRegistry weaponRegistry,
                                 CollisionSystem collisionSystem,
                                 ProjectileSystem projectileSystem,
                                 PickupSpawnSystem pickupSpawnSystem) {
        this.state             = state;
        this.weaponRegistry    = weaponRegistry;
        this.collisionSystem   = collisionSystem;
        this.projectileSystem  = projectileSystem;
        this.pickupSpawnSystem = pickupSpawnSystem;
    }

    public void tick(float dt, Map<String, InputMessage> latestInput) {
        state.tick++;

        pickupSpawnSystem.update(dt);

        for (PlayerState p : state.players.values()) {
            if (p.isDead) {
                p.respawnTimer -= dt;
                if (p.respawnTimer <= 0f) respawnPlayer(p);
                continue;
            }

            collisionSystem.tickSpeedBoost(p, dt);

            InputMessage in   = latestInput.get(p.playerId);
            Vec2 move         = (in != null && in.move != null) ? in.move : Vec2.zero();
            Vec2 aim          = (in != null && in.aim  != null) ? in.aim  : Vec2.zero();

            collisionSystem.applyMovement(p, move, aim, dt);

            // Shoot cooldown
            p.shootCooldownSeconds = Math.max(0f, p.shootCooldownSeconds - dt);

            // Weapon switch (edge-detected on seq number)
            if (in != null && in.switchWeapon && in.seq > p.lastSwitchSeq) {
                p.lastSwitchSeq      = in.seq;
                WeaponType next      = weaponRegistry.nextInCycle(p.equippedWeaponType);
                p.equippedWeaponType = next;
                p.equippedAmmo       = weaponRegistry.get(next).maxAmmo;
                p.shootCooldownSeconds = 0f;
            }

            // Shooting
            if (in != null && in.shoot
                    && p.shootCooldownSeconds <= 0f
                    && p.equippedAmmo > 0) {
                var spec = weaponRegistry.get(p.equippedWeaponType);
                spawnProjectiles(p, spec);
                p.equippedAmmo        -= 1;
                p.shootCooldownSeconds = 1f / spec.fireRate;
            }

            // Trap damage
            if (state.isTrapAtWorld(p.pos.x, p.pos.y)) {
                p.hp = Math.max(0f, p.hp - 10f * dt);
                if (p.hp <= 0f && !p.isDead) {
                    p.isDead      = true;
                    p.respawnTimer = RESPAWN_SECONDS;
                }
            }
        }

        projectileSystem.update(dt);
    }

    // ---- Private ----

    private void respawnPlayer(PlayerState p) {
        Vec2 spawn = state.findNextSpawn();
        p.pos                  = spawn;
        p.hp                   = 100f;
        p.vel                  = Vec2.zero();
        p.equippedWeaponType   = WeaponType.CROSSBOW;
        p.equippedAmmo         = weaponRegistry.get(WeaponType.CROSSBOW).maxAmmo;
        p.shootCooldownSeconds = 0f;
        p.isDead               = false;
        p.respawnTimer         = 0f;
        p.moveSpeed            = PlayerState.BASE_MOVE_SPEED;
        p.speedBoostTimer      = 0f;
        System.out.println("[GAME] " + p.username + " respawned at " + spawn);
    }

    private void spawnProjectiles(PlayerState p, com.mygame.server.domain.model.WeaponSpec spec) {
        float sx = p.pos.x + p.facing.x * (p.radius + 0.15f);
        float sy = p.pos.y + p.facing.y * (p.radius + 0.15f);
        int pellets = Math.max(1, spec.pellets);

        for (int k = 0; k < pellets; k++) {
            float fx = p.facing.x;
            float fy = p.facing.y;

            if (spec.spreadRadians > 0f) {
                float angle  = (float) Math.atan2(fy, fx);
                float jitter = (float) (Math.random() * 2 - 1) * spec.spreadRadians;
                fx = (float) Math.cos(angle + jitter);
                fy = (float) Math.sin(angle + jitter);
            }

            state.projectiles.add(new ProjectileState(
                    UUID.randomUUID().toString(),
                    p.playerId,
                    new Vec2(sx, sy),
                    new Vec2(fx * spec.projectileSpeed, fy * spec.projectileSpeed),
                    spec.damage,
                    spec.projectileRadius,
                    spec.ttlSeconds
            ));
        }
    }
}
