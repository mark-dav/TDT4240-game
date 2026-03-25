package com.mygame.server.domain.system;

import com.mygame.server.data.weapon.WeaponRegistry;
import com.mygame.server.domain.model.PickupState;
import com.mygame.server.domain.model.PlayerState;
import com.mygame.server.domain.model.ServerGameState;
import com.mygame.shared.dto.PickupType;
import com.mygame.shared.dto.WeaponType;
import com.mygame.shared.util.Vec2;

import java.util.Random;
import java.util.UUID;

/**
 * Spawns pickups on a timer and applies their effect when a player walks over one.
 * Separated from the tick orchestrator so pickup rules can change independently.
 */
public final class PickupSpawnSystem {

    private static final int   MAX_PICKUPS    = 8;
    private static final float SPAWN_INTERVAL = 10f;
    private static final float COLLECT_RADIUS = 0.55f;

    private final ServerGameState state;
    private final WeaponRegistry  weaponRegistry;
    private final Random          rng;

    private float spawnTimer = 3f; // first pickup after 3 s

    public PickupSpawnSystem(ServerGameState state, WeaponRegistry weaponRegistry, Random rng) {
        this.state          = state;
        this.weaponRegistry = weaponRegistry;
        this.rng            = rng;
    }

    public void update(float dt) {
        // Spawning
        spawnTimer -= dt;
        if (spawnTimer <= 0f) {
            spawnTimer = SPAWN_INTERVAL;
            if (state.pickups.size() < MAX_PICKUPS) spawnPickup();
        }

        // Collection (only alive players)
        for (PlayerState p : state.players.values()) {
            if (!p.isDead) collectPickups(p);
        }
    }

    // ---- Private ----

    private void spawnPickup() {
        Vec2       pos  = state.randomFloorTile(rng);
        String     id   = UUID.randomUUID().toString();
        PickupType type = randomType();
        PickupState pickup;

        switch (type) {
            case HEALTH:
                pickup = new PickupState(id, PickupType.HEALTH, pos, 40, 0f, null, 0);
                break;
            case SPEED:
                pickup = new PickupState(id, PickupType.SPEED, pos, 0, 5f, null, 0);
                break;
            case WEAPON:
            default:
                WeaponType wt = weaponRegistry.getSwitchOrder()
                        .get(rng.nextInt(weaponRegistry.getSwitchOrder().size()));
                pickup = new PickupState(id, PickupType.WEAPON, pos, 0, 0f, wt,
                        weaponRegistry.get(wt).maxAmmo);
                break;
        }

        state.pickups.add(pickup);
        System.out.println("[PICKUP] Spawned " + type + " at " + pos);
    }

    private void collectPickups(PlayerState p) {
        state.pickups.removeIf(pickup -> {
            if (pickup.pos == null) return true;
            float dx = p.pos.x - pickup.pos.x;
            float dy = p.pos.y - pickup.pos.y;
            if (dx * dx + dy * dy > COLLECT_RADIUS * COLLECT_RADIUS) return false;
            applyPickup(p, pickup);
            System.out.println("[PICKUP] " + p.username + " collected " + pickup.type);
            return true;
        });
    }

    private void applyPickup(PlayerState p, PickupState pickup) {
        switch (pickup.type) {
            case HEALTH:
                p.hp = Math.min(100f, p.hp + pickup.healthAmount);
                break;
            case SPEED:
                p.speedBoostTimer = pickup.speedBoostSeconds;
                p.moveSpeed       = PlayerState.BASE_MOVE_SPEED * 1.6f;
                break;
            case WEAPON:
                if (pickup.weaponType != null) {
                    p.equippedWeaponType = pickup.weaponType;
                    p.equippedAmmo = pickup.ammoAmount > 0
                            ? pickup.ammoAmount
                            : weaponRegistry.get(pickup.weaponType).maxAmmo;
                    p.shootCooldownSeconds = 0f;
                }
                break;
        }
    }

    private PickupType randomType() {
        int r = rng.nextInt(10);
        if (r < 4) return PickupType.HEALTH;
        if (r < 7) return PickupType.SPEED;
        return PickupType.WEAPON;
    }
}
